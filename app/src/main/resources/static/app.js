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
    const username = document.getElementById('username').value;//value:インプット要素で入力された値を取得
    const password = document.getElementById('password').value;
    const bio = document.getElementById('bio').value;
    try{
        //fetchでサーバー側と通信する
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            header: {'Content-Type': 'application/json'},
            body: JSON.stringify({username, password, bio})
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