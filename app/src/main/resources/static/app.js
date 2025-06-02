//ログイン状態を管理するグローバル関数
let currentToken = null;
let currentUser = null;

//ステータスを表示する関数
function showStatus(message, isError = false) {
    const statusDiv = document.getElementById('status-message');
    statusDiv.innerHTML = `<div class="${isError ? 'error' : 'success'} status">%{message}</div>`;//ステータス置き換え
    setTimeout(() => {
        statusDiv.innerHTML = '';
    }, 3000)//3秒後にinnerHTMLを空に
}

//ユーザー登録処理
async function userRegister(evt) {
    evt.preventDefault();//ページリロードを防ぐ

    const username = document.getElementById('username').value;//value:インプット要素で入力された値を取得
    const password = document.getElementById('password').value;
    const bio = document.getElementById('bio').value;

    try{
        //fetchでサーバー側と通信する
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            header: {'Content-Type': 'application/json'},
            body: JSON.stringify({username, password, bio})//オブジェクトをJSONに変換
        });
        if (response.ok){
            const result = await response.json();//サーバーから返ってきたjsonデータを取得
            showStatus(`登録成功： ${result.username}さん、ようこそ`);
            document.getElementById('register-form').reset();
        }else{
            const error = await response.text();//エラー時はテキストで変換される
            showStatus(`エラー：${error}` true);//ステータスをエラーにして返す
        }
    }catch (error){//通信エラー
        showStatus(`エラー：${error.message}` true);
    }
}

//ログイン処理
async function userLogin(evt){
    evt.preventDefault();//ページリロードを防ぐ

    const username = document.getElementById('login-username').value;//value:インプット要素で入力された値を取得
    const password = document.getElementById('login-password').value;

   try{
        //fetchでサーバー側と通信する
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            header: {'Content-Type': 'application/json'},
            body: JSON.stringify({username, password})
        });

        if (response.ok){
            const result = await response.json();//サーバーから返ってきたjsonデータを取得
            currentToken = result.token;//サーバーからのjwtトークン
            currentUser = result;

            showMainSection();//画面切り替え
            showStatus(`ログイン成功： こんにちは、${result.username}さん`);
            loadEvents();//イベント一覧読み込み
        }else{
            const error = await response.text();//エラー時はテキストで変換される
            showStatus(`エラー：${error}` true);//ステータスをエラーにして返す
        }
    }catch (error){//通信エラー
        showStatus(`エラー：${error.message}` true);
    }
}

//イベントの一覧読み込み処理
async function loadEvents(){
    try{
        const response = await fetch('/api/events');
        const events = await response.json();
        const eventsList = document.getElementById('events-list"');//htmlからイベントリスト取得

        //イベントがなかった場合
        if (events.length === 0) {
            eventsList.innerHTML = '<p>現在イベントはありません</p>';
            return;
        }
        eventsList.innerHTML = events.map(event => createEventCard(event)).json('');//イベントカードを設定し文字列へ
    }catch(error){
        showStatus(`イベント一覧読み込み失敗：${error.message}` true);
    }
}

//イベントカードをhtmlで作成して返す
function createEventCard(){
    return `
    <div class="event-card">
    <h3>${event.title}</h3>
    <p>${event.description}</p>
    <p>日時: ${new Date(event.eventDate).toLocaleString()}</p>//サーバー→Dateオブジェクト変換→日本語表示変換
    <p>作成者: ${event.createdBy}</p>
    </div>`;
}

//イベント作成処理
async function createEvent(ent){
    evt.preventDefault();//ページリロードを防ぐ
    const title = document.getElementById('event-title').value;
    const description = document.getElementById('event-description').value;
    const date = document.getElementById('event-date').value;

    //未ログインの場合
    if (!currentToken) {
        showStatus('ログインが必要です', true);
        return;
    }

    try{
        //新しいイベント作成
        const response = await fetch('/api/events', {
            method: 'POST',
            header: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`//認証ヘッダー
            },
            body: {title: title, description: description, eventDate, eventDate}
        });

        if (response.ok){
            const result = await response.json();
            showStatus('イベントが作成されました');
            document.getElementById('create-event-form').reset();//フォーム要素取得してフォームクリア
            loadEvents();//新しく作成したイベントをサーバーから最新取得と画面に反映
        }else{
            showStatus('イベント作成に失敗しました', true);
        }
    }catch(error){
        showStatus(`エラー：${error.message}`, true);
    }
}

//ログイン状態に応じてUIを切り替える関数
function updateUI(){

}

//画面切り替え用の関数
function showMainSection() {
    document.getElementById('auth-section').classList.add('hidden');//ログイン画面を隠す
    document.getElementById('eventRegi-section').classList.remove('hidden');//メイン画面を表示
}
