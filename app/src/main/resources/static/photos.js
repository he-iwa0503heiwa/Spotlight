let currentToken = 'dummy-token';//トークン保持
let currentUser = {id: 1, username: 'testUser'};//ユーザー情報
let selectedEventId = null;//選択されたID
let selectedPhotos = [];//選択された写真

//ページ読み込み時に初期化(HTMLが準備完了次第)
document.addEventListener('DOMContentLoaded', function(){
    loadEvent();
    setupEventListeners();
});

//イベントリスナーの設定
function setupEventListeners() {
    const uploadArea = document.getElementById('upload-area');//アップロード（エリア）
    const fileInput = document.getElementById('file-input');//ファイル選択
    const uploadForm = document.getElementById('upload-form');//アップロードフォーム
    const eventSelect = document.getElementById('event-select');//イベント選択
    const modalClose = document.getElementById('modal-close');//モーダル（写真拡大表示）

    //イベント選択
    eventSelect.addEventListener('change', function()){
        selectedEventId = this.value;
        if (selectedEventId) {
            document.getElementById('upload-section').style.display = 'block';
            document.getElementById('photos-section').style.display = 'block';
            loadPhotos();
        } else {
            document.getElementById('upload-section').style.display = 'none';
            document.getElementById('photos-section').style.display = 'none';
        }
    }
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

//イベント一覧読み込み
async function loadEvent(){
    try{
        const events = [
            {id: 1, title: '阪神戦'},
            {id: 2, title: 'カメラ'},
        ];
        const eventSelect = document.getElementById('event-select');
        events.foreach(event => {
            const option = document.getElementById('option');
            option.value = event.id;
            option.textContent = event.title;
            eventSelect.appendChild(option);
        })
    } catch (error){
        showStatus('イベント一覧の読み込みに失敗しました', true);
    }
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

    if (!selectedEventId) {
        showStatus('イベントを選択してください', true);
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

            //実際は以下のAPIを呼び出し
            /*
            const response = await fetch(`/api/photos/upload/${selectedEventId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${currentToken}`
                },
                body: formData
            });

            if (!response.ok) {
                throw new Error(`${file.name} のアップロードに失敗しました`);
            }
            */

            //ダミーの遅延（実際のアップロード時間をシミュレート）
            await new Promise(resolve => setTimeout(resolve, 500));
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
        //ダミーデータ（実際はAPI呼び出し）
        const photos = [
            {
                id: 1,
                filename: 'photo1.jpg',
                originalFilename: 'game_action.jpg',
                caption: 'ホームランの瞬間！',
                uploadedAt: new Date().toISOString(),
                uploadedBy: { username: 'baseball_fan' },
                fileSize: 2048576
            },
            {
                id: 2,
                filename: 'photo2.jpg',
                originalFilename: 'stadium_view.jpg',
                caption: '甲子園球場の美しい夕景',
                uploadedAt: new Date().toISOString(),
                uploadedBy: { username: 'photo_lover' },
                fileSize: 3145728
            }
        ];

        displayPhotos(photos);

    } catch (error) {
        showStatus('写真一覧の読み込みに失敗しました', true);
    }
}

//写真一覧表示
function displayPhotos(photos) {
    const photosGrid = document.getElementById('photos-grid');
    photosGrid.innerHTML = '';

    if (photos.length === 0) {
        photosGrid.innerHTML = '<p>まだ写真がアップロードされていません。</p>';
        return;
    }

    photos.forEach(photo => {
        const photoCard = document.createElement('div');
        photoCard.className = 'photo-card';

        //削除ボタンの表示判定（投稿者本人のみ）
        const deleteButton = photo.uploadedBy.username === currentUser.username
            ? `<button class="delete-btn" onclick="deletePhoto(${photo.id})">削除</button>`
            : '';

        photoCard.innerHTML = `
            <img src="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD"
                 class="photo-image"
                 alt="${photo.caption}"
                 onclick="openModal('data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD')">
            <div class="photo-info">
                <div class="photo-caption">${photo.caption || '説明なし'}</div>
                <div class="photo-meta">投稿者: ${photo.uploadedBy.username}</div>
                <div class="photo-meta">ファイル名: ${photo.originalFilename}</div>
                <div class="photo-meta">サイズ: ${formatFileSize(photo.fileSize)}</div>
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

    try {
        //実際は以下のAPIを呼び出し
        /*
        const response = await fetch(`/api/photos/${photoId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        if (!response.ok) {
            throw new Error('写真の削除に失敗しました');
        }
        */

        showStatus('写真を削除しました');
        loadPhotos(); //写真一覧を再読み込み

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