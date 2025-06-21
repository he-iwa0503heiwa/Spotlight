//ログイン状態を管理するグローバル関数
let currentToken = null;
let currentUser = null;

//ステータスを表示する関数
function showStatus(message, isError = false) {
    const statusDiv = document.getElementById('status-message');
    statusDiv.innerHTML = `<div class="${isError ? 'error' : 'success'} status">${message}</div>`;//ステータス置き換え
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

    console.log('登録処理開始:', { username, password, bio }); //デバッグログ追加取得用

    try{
        //fetchでサーバー側と通信する
        console.log('APIリクエスト送信前'); // デバッグログ追加
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username, password, bio})//オブジェクトをJSONに変換
        });
        console.log('APIレスポンス受信:', response.status); //デバッグログ追加取得用

        if (response.ok){
            const result = await response.json();//サーバーから返ってきたjsonデータを取得
            console.log('登録成功:', result); //デバッグログ追加取得用
            showStatus(`登録成功： ${result.username}さん、ようこそ`);
            document.getElementById('register-form').reset();
        }else{
            const error = await response.text();//エラー時はテキストで変換される
            console.log('登録エラー:', error); //デバッグログ追加取得用
            showStatus(`エラー：${error}`, true);//ステータスをエラーにして返す
        }
    }catch (error){//通信エラー
        console.log('例外発生:', error); //デバッグログ追加取得用
        showStatus(`エラー：${error.message}`, true);
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
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username, password})
        });

        if (response.ok){
            const result = await response.json();//サーバーから返ってきたjsonデータを取得
            currentToken = result.token;//サーバーからのjwtトークン
            currentUser = result;

            showMainSection();//メイン画面に切り替え
            showStatus(`ログイン成功： こんにちは、${result.username}さん`);
            loadEvents();//イベント一覧読み込み
        }else{
            const error = await response.text();//エラー時はテキストで変換される
            showStatus(`エラー：${error}`, true);//ステータスをエラーにして返す
        }
    }catch (error){//通信エラー
        showStatus(`エラー：${error.message}`, true);
    }
}

//イベントの一覧読み込み処理
async function loadEvents(){
    try{
        const response = await fetch('/api/events');
        const events = await response.json();
        const eventsList = document.getElementById('events-list');//htmlからイベントリスト取得

        //イベントがなかった場合
        if (events.length === 0) {
            eventsList.innerHTML = '<p>現在イベントはありません</p>';
            return;
        }
        //ログイン済みの場合は参加状況も取得してイベントカードを作成
        if (currentToken) {
            //複数非同期処理を並行実行し、eventsWithParticipationに格納
            const eventsWithParticipation = await Promise.all(
                events.map(async (event) => {//mapで配列作成し個々のイベントに非同期処理
                    const participationStatus = await checkParticipationStatus(event.id);//各イベントに参加しているか
                    return { ...event, isParticipating: participationStatus };//イベント情報と参加情報を返す
                })
            );
            //イベントカードに参加状況付きイベントを表示
            eventsList.innerHTML = eventsWithParticipation.map(event => createEventCard(event)).join('');
        } else {
            //ログインしてない場合は、参加状況チェックなしで、イベントカード作成
            eventsList.innerHTML = events.map(event => createEventCard(event)).join('');
        }

    }catch(error){
        showStatus(`イベント一覧読み込み失敗：${error.message}`, true);
    }
}

//イベントカードをhtmlで作成して返す
function createEventCard(evt){
    const createdBy = evt.creator ? evt.creator.username : '不明';

    // 参加ボタンを作成（ログイン済みの場合のみ）
    let participationButton = '';
    if (currentToken) {
        //既に参加しているか
        if (evt.isParticipating) {
            participationButton = `<button class="cancel-btn" onclick="cancelParticipation(${evt.id})">参加キャンセル</button>`;
        } else {
            participationButton = `<button class="participate-btn" onclick="participateEvent(${evt.id})">参加する</button>`;
        }
    }

    return `
    <div class="event-card">
        <h3>${evt.title}</h3>
        <p>${evt.description || ''}</p>
        <p>日時: ${new Date(evt.eventDate).toLocaleString()}</p>
        <p>場所: ${evt.location || '未設定'}</p>
        <p>カテゴリ: ${evt.category ? evt.category.name : '未設定'}</p>
        <p>作成者: ${createdBy}</p>
        <p>参加者数: ${evt.participantCount || 0}/${evt.capacity || '制限なし'}</p>
        ${participationButton}
    </div>`;
}

//イベント作成処理
async function createEvent(evt){
    evt.preventDefault();//ページリロードを防ぐ
    const title = document.getElementById('event-title').value;
    const description = document.getElementById('event-description').value;
    const eventDate = document.getElementById('event-date').value;
    const location = document.getElementById('event-location').value;
    const categoryId = document.getElementById('event-category').value;
    const capacity = document.getElementById('event-capacity').value;

    //未ログインの場合
    if (!currentToken) {
        showStatus('ログインが必要です', true);
        return;
    }

    try{
        //新しいイベント作成
        const response = await fetch('/api/events', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`//認証ヘッダー
            },
            body: JSON.stringify({
                title: title,
                description: description,
                eventDate: eventDate,
                location: location,
                categoryId: parseInt(categoryId),
                capacity: capacity ? parseInt(capacity) : null
            })
        });

        if (response.ok){
            const result = await response.json();
            showStatus('イベントが作成されました');
            document.getElementById('create-event-form').reset();//フォーム要素取得してフォームクリア
            loadEvents();//新しく作成したイベントをサーバーから最新取得と画面に反映
        }else{
            const error = await response.text();
            showStatus(`イベント作成に失敗しました: ${error}`, true);
        }
    }catch(error){
        showStatus(`エラー：${error.message}`, true);
    }
}

//参加状況をチェックする関数（特定のイベントにユーザーが参加しているか）
async function checkParticipationStatus(eventId){
    if(!currentToken) return false;

    try {
        const response = await fetch(`/api/events/${eventId}/participation-status`, {
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        //参加している時
        if (response.ok) {
            const result = await response.json();
            return result.participating;
        }
        return false;
    } catch (error) {
        console.log('参加状況確認エラー:', error);
        return false;
    }
}

//イベント参加処理
async function participateEvent(eventId) {
    if (!currentToken) {
        showStatus('ログインが必要です', true);
        return;
    }

    //参加処理
    try {
        const response = await fetch(`/api/events/${eventId}/participate`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        //参加できた時
        if (response.ok) {
            const result = await response.json();
            showStatus(`イベントに参加しました。ステータス: ${result.status}`);
            loadEvents(); //イベント一覧を再読み込みしてボタンを更新
        } else {
            const error = await response.text();
            showStatus(`参加に失敗しました: ${error}`, true);
        }
    } catch (error) {
        showStatus(`エラー：${error.message}`, true);
    }
}

//イベント参加キャンセル処理
async function cancelParticipation(eventId) {
    if (!currentToken) {
        showStatus('ログインが必要です', true);
        return;
    }

    //キャンセル処理
    try {
        const response = await fetch(`/api/events/${eventId}/participate`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        if (response.ok) {
            showStatus('参加をキャンセルしました');
            loadEvents(); //イベント一覧を再読み込みしてボタンを更新
        } else {
            const error = await response.text();
            showStatus(`キャンセルに失敗しました: ${error}`, true);
        }
    } catch (error) {
        showStatus(`エラー：${error.message}`, true);
    }
}

/*
ログイン状態に応じてUIを切り替える関数
*/
//メイン画面へ切り替え
function showMainSection() {
    document.getElementById('auth-section').classList.add('hidden');//ログイン画面を隠す
    document.getElementById('eventRegi-section').classList.remove('hidden');//メイン画面を表示
}
//認証画面(ログイン)へ切り替え
function showAuthSection(){
    document.getElementById('auth-section').classList.remove('hidden');//ログイン画面を表示
    document.getElementById('eventRegi-section').classList.add('hidden');//メイン画面を隠す
}

//ログアウト
function logout(){
    currentToken = null;
    currentUser = null;

    showAuthSection();
    document.getElementById('login-form').reset();
    showStatus('ログアウトしました');
}