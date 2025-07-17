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

    }
}