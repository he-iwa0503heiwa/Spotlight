let currentToken = null;
let currentUser = null;
let selectedEventId = null;
let selectedEventTitle = '';
let selectedFiles = [];

//ページ読み込み時に初期化
document.addEventListener('DOMContentLoaded', function(){
    initializePage();
    setupEventListeners();
});

//ページ初期化
function initializePage() {
    //セッションストレージからログイン情報を復元
    currentToken = sessionStorage.getItem('currentToken');
    const userJson = sessionStorage.getItem('currentUser');
    if (userJson) {
        currentUser = JSON.parse(userJson);
    }

    // イベント情報を復元
    selectedEventId = sessionStorage.getItem('selectedEventId');
    selectedEventTitle = sessionStorage.getItem('selectedEventTitle') || 'イベント';

    // イベントタイトルを表示
    document.getElementById('event-title').textContent = `${selectedEventTitle} の写真`;

    // ログイン状態をチェック
    if (!currentToken) {
        document.getElementById('login-required').classList.remove('hidden');
        document.getElementById('upload-section').style.display = 'none';
    }

    //写真一覧を読み込み
    if (selectedEventId) {
        loadPhotos();
    } else {
        showStatus('イベント情報が見つかりません', true);
    }
}

//イベントリスナーの設定
function setupEventListeners() {
    const uploadArea = document.getElementById('upload-area');
    const fileInput = document.getElementById('file-input');
    const uploadForm = document.getElementById('upload-form');
    const modalClose = document.getElementById('modal-close');

    //ファイルアップロード関連
    uploadArea.addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', handleFileSelect);
    uploadForm.addEventListener('submit', uploadPhotos);

    //ドラッグ&ドロップ
    uploadArea.addEventListener('dragover', handleDragOver);
    uploadArea.addEventListener('dragleave', handleDragLeave);
    uploadArea.addEventListener('drop', handleDrop);

    //モーダル
    modalClose.addEventListener('click', closeModal);
    window.addEventListener('click', function(event) {
        const modal = document.getElementById('photo-modal');
        if (event.target === modal) {
            closeModal();
        }
    });
}

//ファイル選択処理
function handleFileSelect(event) {
    const files = Array.from(event.target.files);
    processFiles(files);
}

//ドラッグオーバー処理
function handleDragOver(event) {
    event.preventDefault();
    event.currentTarget.classList.add('dragover');
}

//ドラッグリーブ処理
function handleDragLeave(event) {
    event.currentTarget.classList.remove('dragover');
}

//ドロップ処理
function handleDrop(event) {
    event.preventDefault();
    event.currentTarget.classList.remove('dragover');
    const files = Array.from(event.dataTransfer.files);
    processFiles(files);
}

//ファイル処理
function processFiles(files) {
    selectedFiles = files.filter(file => {
        if (!file.type.startsWith('image/')) {
            showStatus(`${file.name} は画像ファイルではありません`, true);
            return false;
        }
        if (file.size > 10 * 1024 * 1024) {
            showStatus(`${file.name} は10MBを超えています`, true);
            return false;
        }
        return true;
    });

    if (selectedFiles.length === 0) return;

    displayPreview();
    document.getElementById('upload-form').style.display = 'block';
}

//プレビュー表示
function displayPreview() {
    const previewArea = document.getElementById('preview-area');
    previewArea.innerHTML = '';

    selectedFiles.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const previewDiv = document.createElement('div');
            previewDiv.innerHTML = `
                <img src="${e.target.result}" class="preview-image" alt="プレビュー">
                <p style="font-size: 14px; color: #666;">${file.name} (${formatFileSize(file.size)})</p>
            `;
            previewArea.appendChild(previewDiv);
        };
        reader.readAsDataURL(file);
    });
}

//写真アップロード
async function uploadPhotos(event) {
    event.preventDefault();

    if (!currentToken) {
        showStatus('ログインが必要です', true);
        return;
    }

    if (!selectedEventId) {
        showStatus('イベント情報が見つかりません', true);
        return;
    }

    if (selectedFiles.length === 0) {
        showStatus('アップロードする写真を選択してください', true);
        return;
    }

    const caption = document.getElementById('caption').value;
    const progressBar = document.getElementById('progress-bar');
    const progressFill = document.getElementById('progress-fill');

    progressBar.style.display = 'block';

    try {
        for (let i = 0; i < selectedFiles.length; i++) {
            const file = selectedFiles[i];
            const formData = new FormData();
            formData.append('file', file);
            formData.append('caption', caption);

            //プログレス更新
            const progress = ((i + 1) / selectedFiles.length) * 100;
            progressFill.style.width = progress + '%';

            const response = await fetch(`/api/photos/upload/${selectedEventId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${currentToken}`
                },
                body: formData
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`${file.name} のアップロードに失敗しました: ${errorText}`);
            }
        }

        showStatus(`${selectedFiles.length}枚の写真をアップロードしました`);
        resetUploadForm();
        loadPhotos(); //写真一覧を再読み込み

    } catch (error) {
        showStatus(`アップロードエラー: ${error.message}`, true);
    } finally {
        progressBar.style.display = 'none';
        progressFill.style.width = '0%';
    }
}

//アップロードフォームリセット
function resetUploadForm() {
    document.getElementById('upload-form').reset();
    document.getElementById('upload-form').style.display = 'none';
    document.getElementById('preview-area').innerHTML = '';
    document.getElementById('file-input').value = '';
    selectedFiles = [];
}

//写真一覧読み込み
async function loadPhotos() {
    if (!selectedEventId) return;

    try {
        const response = await fetch(`/api/photos/event/${selectedEventId}`);

        if (response.ok) {
            const photos = await response.json();
            displayPhotos(photos);
        } else {
            showStatus('写真一覧の読み込みに失敗しました', true);
        }

    } catch (error) {
        showStatus('写真一覧の読み込みに失敗しました', true);
    }
}

//写真一覧表示
function displayPhotos(photos) {
    const photosGrid = document.getElementById('photos-grid');
    photosGrid.innerHTML = '';

    if (photos.length === 0) {
        photosGrid.innerHTML = '<p style="text-align: center; color: #666; padding: 40px;">まだ写真がアップロードされていません。</p>';
        return;
    }

    photos.forEach(photo => {
        const photoCard = document.createElement('div');
        photoCard.className = 'photo-card';

        //削除ボタンの表示判定（投稿者本人またはイベント作成者のみ）
        const canDelete = currentToken &&
                         currentUser &&
                         photo.uploadedBy &&
                         photo.uploadedBy.username === currentUser.username;

        const deleteButton = canDelete
            ? `<button class="delete-btn" onclick="deletePhoto(${photo.id})">削除</button>`
            : '';

        photoCard.innerHTML = `
            <img src="/api/photos/file/${photo.filename}"
                 class="photo-image"
                 alt="${photo.caption || ''}"
                 onclick="openModal('/api/photos/file/${photo.filename}')"
                 onerror="this.src='data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjBmMGYwIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuODreODvOODieWksei0peOBl+OBvuOBl+OBnzwvdGV4dD48L3N2Zz4='">
            <div class="photo-info">
                <div class="photo-caption">${photo.caption || '説明なし'}</div>
                <div class="photo-meta">投稿者: ${photo.uploadedBy ? photo.uploadedBy.username : '不明'}</div>
                <div class="photo-meta">ファイル名: ${photo.originalFilename || photo.filename}</div>
                <div class="photo-meta">サイズ: ${formatFileSize(photo.fileSize || 0)}</div>
                <div class="photo-meta">投稿日: ${formatDate(photo.uploadedAt)}</div>
                ${deleteButton}
            </div>
        `;

        photosGrid.appendChild(photoCard);
    });
}

//写真削除
async function deletePhoto(photoId) {
    if (!confirm('この写真を削除しますか？')) return;

    if (!currentToken) {
        showStatus('ログインが必要です', true);
        return;
    }

    try {
        const response = await fetch(`/api/photos/${photoId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        if (response.ok) {
            showStatus('写真を削除しました');
            loadPhotos(); //写真一覧を再読み込み
        } else {
            const errorText = await response.text();
            showStatus(`削除エラー: ${errorText}`, true);
        }

    } catch (error) {
        showStatus(`削除エラー: ${error.message}`, true);
    }
}

//モーダル表示
function openModal(imageSrc) {
    const modal = document.getElementById('photo-modal');
    const modalImage = document.getElementById('modal-image');
    modalImage.src = imageSrc;
    modal.style.display = 'block';
}

//モーダル閉じる
function closeModal() {
    document.getElementById('photo-modal').style.display = 'none';
}

//ユーティリティ関数
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function formatDate(dateString) {
    if (!dateString) return '不明';
    return new Date(dateString).toLocaleDateString('ja-JP', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function showStatus(message, isError = false) {
    const statusDiv = document.getElementById('status-message');
    statusDiv.innerHTML = `<div class="${isError ? 'error' : 'success'} status">${message}</div>`;
    setTimeout(() => {
        statusDiv.innerHTML = '';
    }, 3000);
}