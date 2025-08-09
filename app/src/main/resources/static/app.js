//ログイン状態を管理するグローバル関数
let currentToken = null;
let currentUser = null;
let editingEventId = null; // 編集中のイベントID

//ステータスを表示する関数
function showStatus(message, isError = false) {
    const statusDiv = document.getElementById('status-message');
    statusDiv.innerHTML = `<div class="${isError ? 'error' : 'success'} status">${message}</div>`;
    setTimeout(() => {
        statusDiv.innerHTML = '';
    }, 3000);
}

//バリデーションエラーを表示する関数
function showValidationErrors(errors) {
    clearValidationErrors();
    Object.keys(errors).forEach(fieldName => {
        const fieldElement = document.getElementById(fieldName);
        if (fieldElement) {
            fieldElement.style.borderColor = '#e74c3c';
            fieldElement.style.borderWidth = '2px';
            const errorDiv = document.createElement('div');
            errorDiv.className = 'field-error';
            errorDiv.textContent = errors[fieldName];
            errorDiv.style.color = '#e74c3c';
            errorDiv.style.fontSize = '14px';
            errorDiv.style.marginTop = '5px';
            fieldElement.parentNode.appendChild(errorDiv);
        }
    });
}

//バリデーションエラーをクリアする関数
function clearValidationErrors() {
    const errorElements = document.querySelectorAll('.field-error');
    errorElements.forEach(element => element.remove());
    const inputElements = document.querySelectorAll('input, textarea, select');
    inputElements.forEach(element => {
        element.style.borderColor = '#ddd';
        element.style.borderWidth = '1px';
    });
}

//エラーレスポンスを処理する共通関数
async function handleErrorResponse(response) {
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        const errorData = await response.json();
        if (errorData.fieldErrors) {
            showValidationErrors(errorData.fieldErrors);
            showStatus(errorData.message || 'バリデーションエラーがあります', true);
        } else {
            showStatus(errorData.message || 'エラーが発生しました', true);
        }
    } else {
        const errorText = await response.text();
        showStatus(`エラー：${errorText}`, true);
    }
}

//ユーザー登録処理
async function userRegister(evt) {
    evt.preventDefault();
    clearValidationErrors();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const bio = document.getElementById('bio').value;

    try{
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username, password, bio})
        });

        if (response.ok){
            const result = await response.json();
            showStatus(`登録成功： ${result.username}さん、ようこそ`);
            document.getElementById('register-form').reset();
            clearValidationErrors();
        }else{
            await handleErrorResponse(response);
        }
    }catch (error){
        showStatus(`エラー：${error.message}`, true);
    }
}

//ログイン処理
async function userLogin(evt){
    evt.preventDefault();
    clearValidationErrors();

    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

   try{
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username, password})
        });

        if (response.ok){
            const result = await response.json();
            currentToken = result.token;
            currentUser = result;

            showMainSection();
            showStatus(`ログイン成功： こんにちは、${result.username}さん`);
            loadEvents();
            clearValidationErrors();
        }else{
            await handleErrorResponse(response);
        }
    }catch (error){
        showStatus(`エラー：${error.message}`, true);
    }
}

//イベントの一覧読み込み処理
async function loadEvents(){
    try{
        const response = await fetch('/api/events');
        const events = await response.json();
        const eventsList = document.getElementById('events-list');

        if (events.length === 0) {
            eventsList.innerHTML = '<p>現在イベントはありません</p>';
            return;
        }

        if (currentToken) {
            const eventsWithParticipation = await Promise.all(
                events.map(async (event) => {
                    const participationStatus = await checkParticipationStatus(event.id);
                    return { ...event, isParticipating: participationStatus };
                })
            );
            eventsList.innerHTML = eventsWithParticipation.map(event => createEventCard(event)).join('');
        } else {
            eventsList.innerHTML = events.map(event => createEventCard(event)).join('');
        }

    }catch(error){
        showStatus(`イベント一覧読み込み失敗：${error.message}`, true);
    }
}

//イベントカードをhtmlで作成して返す
function createEventCard(evt){
    const createdBy = evt.creator ? evt.creator.username : '不明';

    let participationButton = '';
    if (currentToken) {
        if (evt.isParticipating) {
            participationButton = `<button class="cancel-btn" onclick="cancelParticipation(${evt.id})">参加キャンセル</button>`;
        } else {
            participationButton = `<button class="participate-btn" onclick="participateEvent(${evt.id})">参加する</button>`;
        }
    }

    let managementButtons = '';
    if (currentToken && currentUser && evt.creator && evt.creator.id === currentUser.id) {
        managementButtons = `
            <div class="management-buttons">
                <button class="edit-btn" onclick="startEditEvent(${evt.id})">編集</button>
                <button class="delete-btn" onclick="deleteEvent(${evt.id})">削除</button>
            </div>
        `;
    }

    const eventTitleEscaped = evt.title.replace(/'/g, "\\'");
    const photoButton = `<button class="photo-btn" onclick="goToPhotosPage(${evt.id}, '${eventTitleEscaped}')">写真を見る</button>`;

    return `
    <div class="event-card" id="event-card-${evt.id}">
        <h3>${evt.title}</h3>
        <p>${evt.description || ''}</p>
        <p>日時: ${new Date(evt.eventDate).toLocaleString()}</p>
        <p>場所: ${evt.location || '未設定'}</p>
        <p>カテゴリ: ${evt.category ? evt.category.name : '未設定'}</p>
        <p>作成者: ${createdBy}</p>
        <p>参加者数: ${evt.participantCount || 0}/${evt.capacity || '制限なし'}</p>
        <div class="button-container">
            ${participationButton}
            ${photoButton}
            ${managementButtons}
        </div>
    </div>`;
}

//写真ページに遷移する関数
function goToPhotosPage(eventId, eventTitle) {
    if (currentToken) {
        sessionStorage.setItem('currentToken', currentToken);
        sessionStorage.setItem('currentUser', JSON.stringify(currentUser));
    }
    sessionStorage.setItem('selectedEventId', eventId);
    sessionStorage.setItem('selectedEventTitle', eventTitle || '');
    window.location.href = 'photos.html';
}

//イベント作成処理
async function createEvent(evt){
    evt.preventDefault();
    clearValidationErrors();

    const title = document.getElementById('event-title').value;
    const description = document.getElementById('event-description').value;
    const eventDate = document.getElementById('event-date').value;
    const location = document.getElementById('event-location').value;
    const categoryId = document.getElementById('event-category').value;
    const capacity = document.getElementById('event-capacity').value;

    if (!currentToken) {
        showStatus('ログインが必要です', true);
        return;
    }

    try{
        let url = '/api/events';
        let method = 'POST';
        let successMessage = 'イベントが作成されました';

        if (editingEventId) {
            url = `/api/events/${editingEventId}`;
            method = 'PUT';
            successMessage = 'イベントが更新されました';
        }

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
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
            showStatus(successMessage);
            document.getElementById('create-event-form').reset();
            cancelEditEvent();
            clearValidationErrors();
            loadEvents();
        }else{
            await handleErrorResponse(response);
        }
    }catch(error){
        showStatus(`エラー：${error.message}`, true);
    }
}

//参加状況をチェックする関数
async function checkParticipationStatus(eventId){
    if(!currentToken) return false;
    try {
        const response = await fetch(`/api/events/${eventId}/participation-status`, {
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });
        if (response.ok) {
            const result = await response.json();
            return result.participating;
        }
        return false;
    } catch (error) {
        return false;
    }
}

//イベント参加処理
async function participateEvent(eventId) {
    if (!currentToken) {
        showStatus('ログインが必要です', true);
        return;
    }
    try {
        const response = await fetch(`/api/events/${eventId}/participate`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });
        if (response.ok) {
            const result = await response.json();
            showStatus(`イベントに参加しました。ステータス: ${result.status}`);
            loadEvents();
        } else {
            await handleErrorResponse(response);
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
    try {
        const response = await fetch(`/api/events/${eventId}/participate`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });
        if (response.ok) {
            showStatus('参加をキャンセルしました');
            loadEvents();
        } else {
            await handleErrorResponse(response);
        }
    } catch (error) {
        showStatus(`エラー：${error.message}`, true);
    }
}

//編集関連の簡易関数
function startEditEvent(eventId) {
    showStatus('編集機能は準備中です');
}

function cancelEditEvent() {
    editingEventId = null;
}

function deleteEvent(eventId) {
    if (!confirm('本当にこのイベントを削除しますか？')) {
        return;
    }
    showStatus('削除機能は準備中です');
}

//UI切り替え関数
function showMainSection() {
    document.getElementById('auth-section').classList.add('hidden');
    document.getElementById('eventRegi-section').classList.remove('hidden');
}

function showAuthSection(){
    document.getElementById('auth-section').classList.remove('hidden');
    document.getElementById('eventRegi-section').classList.add('hidden');
    clearValidationErrors();
}

function logout(){
    currentToken = null;
    currentUser = null;
    editingEventId = null;
    showAuthSection();
    document.getElementById('login-form').reset();
    document.getElementById('create-event-form').reset();
    clearValidationErrors();
    showStatus('ログアウトしました');
}

//ユーザー情報を取得する関数
async function loadUserInfo() {
    if (!currentToken) {
        showStatus('ログインが必要です', true);
        return;
    }

    try {
        const response = await fetch('/api/users/me', {
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        if (response.ok) {
            const userInfo = await response.json();
            displayUserInfo(userInfo);
        } else {
            await handleErrorResponse(response);
        }
    } catch (error) {
        showStatus(`エラー：${error.message}`, true);
    }
}

//ユーザー情報を表示する関数
function displayUserInfo(userInfo) {
    const userInfoDiv = document.getElementById('user-info');
    const createdDate = new Date(userInfo.createdAt).toLocaleDateString();

    userInfoDiv.innerHTML = `
        <div class="user-info-card">
            <h3>ユーザー情報</h3>
            <p><strong>ID:</strong> ${userInfo.id}</p>
            <p><strong>ユーザー名:</strong> ${userInfo.username}</p>
            <p><strong>自己紹介:</strong> ${userInfo.bio || '未設定'}</p>
            <p><strong>登録日:</strong> ${createdDate}</p>
        </div>
    `;
}

//マイページを表示する関数（完全版）
function showMyPage() {
    // 他のセクションを隠す
    document.getElementById('events-list-section').classList.add('hidden');
    document.getElementById('create-event-section').classList.add('hidden');
    document.getElementById('my-page-section').classList.remove('hidden');

    //バリデーションエラーをクリア
    clearValidationErrors();

    //ユーザー情報と参加イベントを読み込み
    loadUserInfo();
    loadMyParticipations();
}

//イベント一覧画面に戻る関数
function showEventsList() {
    document.getElementById('my-page-section').classList.add('hidden');
    document.getElementById('events-list-section').classList.remove('hidden');
    document.getElementById('create-event-section').classList.remove('hidden');

    //編集モードをキャンセル
    if (editingEventId) {
        document.getElementById('create-event-form').reset();
        cancelEditEvent();
    }
}

//自分の参加イベント一覧を取得する関数
async function loadMyParticipations() {
    if (!currentToken) return;

    try {
        const response = await fetch('/api/events/my-participations', {
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        if (response.ok) {
            const participations = await response.json();
            displayMyParticipations(participations);
        } else {
            await handleErrorResponse(response);
        }
    } catch (error) {
        showStatus(`エラー：${error.message}`, true);
    }
}

//参加イベント一覧を表示する関数
function displayMyParticipations(participations) {
    const participationsDiv = document.getElementById('my-participations');

    if (participations.length === 0) {
        participationsDiv.innerHTML = '<p>まだイベントに参加していません</p>';
        return;
    }

    participationsDiv.innerHTML = participations.map(participation => `
        <div class="participation-card">
            <h4>${participation.eventTitle}</h4>
            <p><strong>ステータス:</strong> ${participation.status}</p>
            <p><strong>参加日:</strong> ${new Date(participation.participatedAt).toLocaleDateString()}</p>
        </div>
    `).join('');
}