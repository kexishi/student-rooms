// 应用状态
let currentUser = null;
let currentRoom = null;
let currentSeat = null;

// 时间段时长（小时）
const SLOT_DURATION = 1;

// 当前可用时间段（根据自习室开放时间动态生成）
let TIME_SLOTS = [];

// 生成整点选项（用于时间选择下拉框）
function generateHourOptions(startHour, endHour, selectedValue) {
    let options = '';
    for (let hour = startHour; hour <= endHour; hour++) {
        const timeValue = `${String(hour).padStart(2, '0')}:00`;
        const selected = timeValue === selectedValue ? 'selected' : '';
        options += `<option value="${timeValue}" ${selected}>${timeValue}</option>`;
    }
    return options;
}

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    checkLoginState();
    loadStudyRooms();
    createTestData();
});

// 检查登录状态
function checkLoginState() {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
        currentUser = JSON.parse(savedUser);
        updateUserUI();
    }
}

// 更新用户界面
function updateUserUI() {
    const userInfo = document.getElementById('user-info');
    if (currentUser) {
        const isAdmin = currentUser.role === 'ADMIN';
        userInfo.innerHTML = `
            <span>欢迎，${currentUser.name} ${isAdmin ? '(管理员)' : ''}</span>
            ${isAdmin ? `<button class="btn btn-small btn-warning" onclick="showAdminReservationManagement()">预约管理</button>` : ''}
            ${isAdmin ? `<button class="btn btn-small btn-primary" onclick="showRoomManagement()">管理自习室</button>` : ''}
            <button class="btn btn-small btn-secondary" onclick="showMyReservations()">我的预约</button>
            <button class="btn btn-small btn-danger" onclick="logout()">退出</button>
        `;
        document.getElementById('add-room-btn').style.display = 'none';
    } else {
        userInfo.innerHTML = `
            <button id="login-btn" onclick="showLogin()">登录</button>
            <button id="register-btn" onclick="showRegister()">注册</button>
        `;
        document.getElementById('add-room-btn').style.display = 'none';
    }
}

// 检查是否是管理员
function isAdmin() {
    return currentUser && currentUser.role === 'ADMIN';
}

// 显示登录弹窗
function showLogin() {
    document.getElementById('modal-content').innerHTML = `
        <h2>用户登录</h2>
        <form id="login-form">
            <div class="form-group">
                <label>用户名</label>
                <input type="text" id="login-username" required>
            </div>
            <div class="form-group">
                <label>密码</label>
                <input type="password" id="login-password" required>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">登录</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
            </div>
        </form>
    `;
    document.getElementById('modal-overlay').classList.add('active');
    document.getElementById('login-form').addEventListener('submit', handleLogin);
}

// 处理登录
async function handleLogin(e) {
    e.preventDefault();
    try {
        const username = document.getElementById('login-username').value;
        const password = document.getElementById('login-password').value;
        currentUser = await UserAPI.login(username, password);
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        updateUserUI();
        closeModal();
        showToast('登录成功！', 'success');
        loadStudyRooms();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 显示注册弹窗
function showRegister() {
    document.getElementById('modal-content').innerHTML = `
        <h2>用户注册</h2>
        <form id="register-form">
            <div class="form-group">
                <label>用户名</label>
                <input type="text" id="reg-username" required>
            </div>
            <div class="form-group">
                <label>密码</label>
                <input type="password" id="reg-password" required>
            </div>
            <div class="form-group">
                <label>姓名</label>
                <input type="text" id="reg-name" required>
            </div>
            <div class="form-group">
                <label>邮箱</label>
                <input type="email" id="reg-email">
            </div>
            <div class="form-group">
                <label>手机号</label>
                <input type="tel" id="reg-phone" pattern="1[3-9]\\d{9}">
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">注册</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
            </div>
        </form>
    `;
    document.getElementById('modal-overlay').classList.add('active');
    document.getElementById('register-form').addEventListener('submit', handleRegister);
}

// 处理注册
async function handleRegister(e) {
    e.preventDefault();
    try {
        const userData = {
            username: document.getElementById('reg-username').value,
            password: document.getElementById('reg-password').value,
            name: document.getElementById('reg-name').value,
            email: document.getElementById('reg-email').value,
            phone: document.getElementById('reg-phone').value
        };
        currentUser = await UserAPI.register(userData);
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        updateUserUI();
        closeModal();
        showToast('注册成功！', 'success');
        loadStudyRooms();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 登出
function logout() {
    currentUser = null;
    localStorage.removeItem('currentUser');
    updateUserUI();
    showStudyRooms();
    showToast('已退出登录', 'info');
}

// 关闭弹窗
function closeModal() {
    document.getElementById('modal-overlay').classList.remove('active');
}

// 加载自习室列表
async function loadStudyRooms() {
    try {
        const rooms = await StudyRoomAPI.getAll();
        renderRooms(rooms);
    } catch (error) {
        showToast('加载自习室失败', 'error');
    }
}

// 存储当前自习室列表
let currentRoomsList = [];

// 渲染自习室列表
function renderRooms(rooms) {
    const container = document.getElementById('rooms-list');
    if (!rooms || rooms.length === 0) {
        container.innerHTML = '<p style="color: #666; text-align: center; padding: 2rem;">暂无自习室</p>';
        return;
    }
    
    // 保存完整的自习室列表
    currentRoomsList = rooms;
    
    container.innerHTML = rooms.map((room, index) => `
        <div class="room-card" onclick="selectRoomByIndex(${index})">
            <h3>[${room.name}]</h3>
            <p>位置: ${room.location || '位置待定'}</p>
            <p>座位: ${room.totalSeats}个</p>
            <p>时间: ${room.openTime} - ${room.closeTime}</p>
            <span class="room-status status-${room.status.toLowerCase()}">
                ${getStatusText(room.status)}
            </span>
        </div>
    `).join('');
}

// 通过索引选择自习室（使用完整的自习室数据）
async function selectRoomByIndex(index) {
    const room = currentRoomsList[index];
    if (!room) return;
    await showSeats(room.id, room.name, room);
}

// 显示新增自习室弹窗
function showAddRoom() {
    document.getElementById('modal-content').innerHTML = `
        <h2>新增自习室</h2>
        <form id="add-room-form">
            <div class="form-group">
                <label>自习室名称</label>
                <input type="text" id="room-name" required>
            </div>
            <div class="form-group">
                <label>位置</label>
                <input type="text" id="room-location">
            </div>
            <div class="form-group">
                <label>描述</label>
                <textarea id="room-desc"></textarea>
            </div>
            <div class="form-group">
                <label>座位数</label>
                <input type="number" id="room-seats" min="1" required>
            </div>
            <div class="form-group">
                <label>开放时间</label>
                <select id="room-open" required>
                    ${generateHourOptions(6, 22, '08:00')}
                </select>
            </div>
            <div class="form-group">
                <label>关闭时间</label>
                <select id="room-close" required>
                    ${generateHourOptions(7, 23, '22:00')}
                </select>
            </div>
            <div class="form-group">
                <label>状态</label>
                <select id="room-status">
                    <option value="OPEN">开放</option>
                    <option value="CLOSED">关闭</option>
                    <option value="MAINTENANCE">维护中</option>
                </select>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">创建</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
            </div>
        </form>
    `;
    document.getElementById('modal-overlay').classList.add('active');
    document.getElementById('add-room-form').addEventListener('submit', handleAddRoom);
}

// 处理新增自习室
async function handleAddRoom(e) {
    e.preventDefault();
    try {
        const roomData = {
            name: document.getElementById('room-name').value,
            location: document.getElementById('room-location').value,
            description: document.getElementById('room-desc').value,
            totalSeats: parseInt(document.getElementById('room-seats').value),
            openTime: document.getElementById('room-open').value,
            closeTime: document.getElementById('room-close').value,
            status: document.getElementById('room-status').value
        };
        await StudyRoomAPI.create(currentUser.id, roomData);
        closeModal();
        showToast('自习室创建成功！', 'success');
        loadStudyRooms();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// ========== 管理员自习室管理功能 ==========

// 显示自习室管理弹窗
async function showRoomManagement() {
    try {
        const rooms = await StudyRoomAPI.getAll();
        
        const roomListHtml = rooms.map(room => `
            <tr>
                <td>${room.name}</td>
                <td>${room.location || '-'}</td>
                <td>${room.totalSeats}</td>
                <td>${room.openTime} - ${room.closeTime}</td>
                <td><span class="status-badge status-${room.status.toLowerCase()}">${getStatusText(room.status)}</span></td>
                <td>
                    <button class="btn btn-small btn-primary" onclick="editRoom(${room.id})">编辑</button>
                    <button class="btn btn-small btn-danger" onclick="deleteRoom(${room.id}, '${room.name}')">删除</button>
                </td>
            </tr>
        `).join('');
        
        document.getElementById('modal-content').innerHTML = `
            <h2>管理自习室</h2>
            <div style="margin-bottom: 15px;">
                <button class="btn btn-primary" onclick="showAddRoom()">+ 新增自习室</button>
            </div>
            <div style="max-height: 400px; overflow-y: auto;">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>名称</th>
                            <th>位置</th>
                            <th>座位数</th>
                            <th>开放时间</th>
                            <th>状态</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${roomListHtml || '<tr><td colspan="6" style="text-align: center;">暂无自习室</td></tr>'}
                    </tbody>
                </table>
            </div>
            <div class="form-actions" style="margin-top: 15px;">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">关闭</button>
            </div>
        `;
        document.getElementById('modal-overlay').classList.add('active');
    } catch (error) {
        showToast('加载自习室列表失败', 'error');
    }
}

// 编辑自习室
async function editRoom(roomId) {
    try {
        const room = await StudyRoomAPI.getById(roomId);
        
        document.getElementById('modal-content').innerHTML = `
            <h2>编辑自习室</h2>
            <form id="edit-room-form">
                <input type="hidden" id="edit-room-id" value="${room.id}">
                <div class="form-group">
                    <label>自习室名称</label>
                    <input type="text" id="edit-room-name" value="${room.name}" required>
                </div>
                <div class="form-group">
                    <label>位置</label>
                    <input type="text" id="edit-room-location" value="${room.location || ''}">
                </div>
                <div class="form-group">
                    <label>描述</label>
                    <textarea id="edit-room-desc">${room.description || ''}</textarea>
                </div>
                <div class="form-group">
                    <label>座位数</label>
                    <input type="number" id="edit-room-seats" value="${room.totalSeats}" min="1" required>
                </div>
                <div class="form-group">
                    <label>开放时间</label>
                    <select id="edit-room-open" required>
                        ${generateHourOptions(6, 22, room.openTime)}
                    </select>
                </div>
                <div class="form-group">
                    <label>关闭时间</label>
                    <select id="edit-room-close" required>
                        ${generateHourOptions(7, 23, room.closeTime)}
                    </select>
                </div>
                <div class="form-group">
                    <label>状态</label>
                    <select id="edit-room-status">
                        <option value="OPEN" ${room.status === 'OPEN' ? 'selected' : ''}>开放</option>
                        <option value="CLOSED" ${room.status === 'CLOSED' ? 'selected' : ''}>关闭</option>
                        <option value="MAINTENANCE" ${room.status === 'MAINTENANCE' ? 'selected' : ''}>维护中</option>
                    </select>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">保存</button>
                    <button type="button" class="btn btn-secondary" onclick="showRoomManagement()">返回</button>
                </div>
            </form>
        `;
        document.getElementById('edit-room-form').addEventListener('submit', handleEditRoom);
    } catch (error) {
        showToast('加载自习室信息失败', 'error');
    }
}

// 处理编辑自习室
async function handleEditRoom(e) {
    e.preventDefault();
    try {
        const roomId = document.getElementById('edit-room-id').value;
        const roomData = {
            name: document.getElementById('edit-room-name').value,
            location: document.getElementById('edit-room-location').value,
            description: document.getElementById('edit-room-desc').value,
            totalSeats: parseInt(document.getElementById('edit-room-seats').value),
            openTime: document.getElementById('edit-room-open').value,
            closeTime: document.getElementById('edit-room-close').value,
            status: document.getElementById('edit-room-status').value
        };
        await StudyRoomAPI.update(roomId, roomData);
        closeModal();
        showToast('自习室更新成功！', 'success');
        loadStudyRooms();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 删除自习室
async function deleteRoom(roomId, roomName) {
    if (!confirm(`确定要删除自习室 "${roomName}" 吗？\n注意：删除后将无法恢复，相关座位和预约记录也会被删除。`)) {
        return;
    }
    
    try {
        await StudyRoomAPI.delete(roomId);
        showToast('自习室删除成功！', 'success');
        showRoomManagement(); // 刷新列表
        loadStudyRooms(); // 刷新主页列表
    } catch (error) {
        showToast(error.message || '删除失败', 'error');
    }
}

// 选择自习室
async function showSeats(roomId, roomName, roomData = null) {
    if (!currentUser) {
        showToast('请先登录', 'info');
        showLogin();
        return;
    }
    
    // 使用传入的完整自习室数据，或获取详细信息
    if (roomData && roomData.openTime && roomData.closeTime) {
        currentRoom = roomData;
    } else {
        // 获取完整的自习室信息（包含开放时间）
        try {
            const roomDetail = await StudyRoomAPI.getById(roomId);
            currentRoom = roomDetail;
        } catch (e) {
            // 如果获取失败，使用基本信息
            currentRoom = { id: roomId, name: roomName };
        }
    }
    
    document.getElementById('room-title').textContent = `${roomName} - 座位选择`;
    
    try {
        const seats = await SeatAPI.getByRoom(roomId);
        renderSeats(seats);
        document.getElementById('study-rooms-section').style.display = 'none';
        document.getElementById('seats-section').style.display = 'block';
    } catch (error) {
        showToast('加载座位失败', 'error');
    }
}

// 渲染座位列表
function renderSeats(seats) {
    const container = document.getElementById('seats-list');
    container.innerHTML = seats.map(seat => `
        <div class="seat-card ${seat.status.toLowerCase()}" onclick="selectSeat(${seat.id}, '${seat.seatNumber}')">
            <div class="seat-number">${seat.seatNumber}</div>
            <div class="seat-status-text">${getSeatStatusText(seat.status)}</div>
            ${seat.hasSocket ? '<div>[有插座]</div>' : ''}
        </div>
    `).join('');
}

// 选择座位
function selectSeat(seatId, seatNumber) {
    currentSeat = { id: seatId, number: seatNumber };
    showReservationForm();
}

// 根据自习室开放时间动态生成1小时时间段
function generateTimeSlots() {
    const slots = [];
    
    if (!currentRoom || !currentRoom.openTime || !currentRoom.closeTime) {
        // 默认生成8:00-22:00的1小时时间段
        for (let hour = 8; hour < 22; hour++) {
            slots.push({
                index: hour - 8,
                label: `${String(hour).padStart(2, '0')}:00-${String(hour + 1).padStart(2, '0')}:00`,
                start: `${String(hour).padStart(2, '0')}:00`,
                end: `${String(hour + 1).padStart(2, '0')}:00`
            });
        }
        return slots;
    }
    
    const openHour = parseInt(currentRoom.openTime.split(':')[0]);
    const closeHour = parseInt(currentRoom.closeTime.split(':')[0]);
    
    let index = 0;
    for (let hour = openHour; hour < closeHour; hour++) {
        slots.push({
            index: index,
            label: `${String(hour).padStart(2, '0')}:00-${String(hour + 1).padStart(2, '0')}:00`,
            start: `${String(hour).padStart(2, '0')}:00`,
            end: `${String(hour + 1).padStart(2, '0')}:00`
        });
        index++;
    }
    
    return slots;
}

// 显示预约表单（1小时时间段，支持多选）
async function showReservationForm() {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const minDate = tomorrow.toISOString().split('T')[0];
    
    // 根据自习室开放时间动态生成1小时时间段
    TIME_SLOTS = generateTimeSlots();
    
    // 获取已预约的时间段
    let occupiedSlots = [];
    try {
        const reservations = await ReservationAPI.getBySeatAndDate(currentSeat.id, minDate);
        console.log('预约记录:', reservations);
        // 后端返回的时间格式是 "08:00:00"，需要截取前5位变成 "08:00"
        occupiedSlots = reservations.map(r => `${r.startTime.substring(0, 5)}-${r.endTime.substring(0, 5)}`);
        console.log('已占用时段:', occupiedSlots);
    } catch (e) {
        console.log('获取已预约信息失败', e);
    }
    
    // 生成时间段选项（支持多选）
    const timeSlotOptions = TIME_SLOTS.map(slot => {
        const slotKey = `${slot.start}-${slot.end}`;
        const isOccupied = occupiedSlots.includes(slotKey);
        console.log(`时段 ${slotKey}: ${isOccupied ? '已占用' : '可用'}`);
        return `
            <label class="time-slot-option ${isOccupied ? 'occupied' : ''}">
                <input type="checkbox" name="timeSlot" value="${slot.index}" ${isOccupied ? 'disabled' : ''}>
                <span>${slot.label}</span>
                ${isOccupied ? '<span class="occupied-label">[已预约]</span>' : ''}
            </label>
        `;
    }).join('');
    
    document.getElementById('modal-content').innerHTML = `
        <h2>预约座位</h2>
        <div style="margin-bottom: 15px; color: #666;">
            <p>自习室: ${currentRoom.name}</p>
            <p>座位: ${currentSeat.number}号</p>
            <p>开放时间: ${currentRoom.openTime || '08:00'} - ${currentRoom.closeTime || '22:00'}</p>
            <p style="color: #856404; font-size: 12px;">每个时间段为1小时，可选择多个连续或不连续时段</p>
        </div>
        <form id="reservation-form">
            <div class="form-group">
                <label>预约日期</label>
                <input type="date" id="res-date" min="${minDate}" value="${minDate}" required onchange="updateTimeSlots()">
            </div>
            <div class="form-group">
                <label>选择时间段（可多选）</label>
                <div class="time-slots" id="time-slots-container" style="max-height: 250px; overflow-y: auto;">
                    ${timeSlotOptions}
                </div>
            </div>
            <div class="form-group">
                <label>备注</label>
                <textarea id="res-remark" rows="2"></textarea>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">确认预约</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
            </div>
        </form>
    `;
    document.getElementById('modal-overlay').classList.add('active');
    document.getElementById('reservation-form').addEventListener('submit', handleReservation);
}

// 根据日期更新时间段
async function updateTimeSlots() {
    const date = document.getElementById('res-date').value;
    if (!date || !currentSeat) return;
    
    try {
        const reservations = await ReservationAPI.getBySeatAndDate(currentSeat.id, date);
        // 后端返回的时间格式是 "08:00:00"，需要截取前5位变成 "08:00"
        const occupiedSlots = reservations.map(r => `${r.startTime.substring(0, 5)}-${r.endTime.substring(0, 5)}`);
        
        // 更新每个选项的状态
        TIME_SLOTS.forEach((slot, index) => {
            const isOccupied = occupiedSlots.includes(`${slot.start}-${slot.end}`);
            const option = document.querySelector(`input[value="${index}"]`);
            if (option) {
                option.disabled = isOccupied;
                option.parentElement.classList.toggle('occupied', isOccupied);
                
                // 更新已预约标记
                let label = option.parentElement.querySelector('.occupied-label');
                if (isOccupied && !label) {
                    option.parentElement.insertAdjacentHTML('beforeend', '<span class="occupied-label">[已预约]</span>');
                } else if (!isOccupied && label) {
                    label.remove();
                }
            }
        });
    } catch (e) {
        console.log('获取已预约信息失败', e);
    }
}

// 处理预约（支持多选时间段，逐个提交）
async function handleReservation(e) {
    e.preventDefault();
    try {
        const selectedSlots = document.querySelectorAll('input[name="timeSlot"]:checked');
        if (selectedSlots.length === 0) {
            showToast('请至少选择一个时间段', 'error');
            return;
        }
        
        const date = document.getElementById('res-date').value;
        const remark = document.getElementById('res-remark').value;
        
        // 逐个提交每个选中的时间段
        let successCount = 0;
        let failCount = 0;
        
        for (const slot of selectedSlots) {
            const reservationData = {
                seatId: currentSeat.id,
                date: date,
                timeSlot: parseInt(slot.value),
                remark: remark
            };
            
            try {
                await ReservationAPI.create(currentUser.id, reservationData);
                successCount++;
            } catch (error) {
                console.log('预约时段失败:', error.message);
                failCount++;
            }
        }
        
        closeModal();
        if (successCount > 0) {
            showToast(`成功预约 ${successCount} 个时段${failCount > 0 ? `，${failCount} 个时段失败` : ''}`, 'success');
            backToRooms();
        } else {
            showToast('预约失败，请重试', 'error');
        }
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 返回自习室列表
function backToRooms() {
    document.getElementById('seats-section').style.display = 'none';
    document.getElementById('reservations-section').style.display = 'none';
    document.getElementById('admin-reservations-section').style.display = 'none';
    document.getElementById('study-rooms-section').style.display = 'block';
    loadStudyRooms();
}

// 显示自习室列表
function showStudyRooms() {
    backToRooms();
}

// 显示我的预约
async function showMyReservations() {
    if (!currentUser) {
        showToast('请先登录', 'info');
        showLogin();
        return;
    }
    
    try {
        const reservations = await ReservationAPI.getUserReservations(currentUser.id);
        renderReservations(reservations);
        document.getElementById('study-rooms-section').style.display = 'none';
        document.getElementById('seats-section').style.display = 'none';
        document.getElementById('reservations-section').style.display = 'block';
    } catch (error) {
        showToast('加载预约失败', 'error');
    }
}

// 渲染预约列表
function renderReservations(reservations) {
    const container = document.getElementById('reservations-list');
    if (!reservations || reservations.length === 0) {
        container.innerHTML = '<p style="color: #666; text-align: center; padding: 2rem;">暂无预约记录</p>';
        return;
    }
    
    container.innerHTML = `
        <div class="reservations-list">
            ${reservations.map(res => `
                <div class="reservation-card">
                    <h4>${res.studyRoomName} - ${res.seatNumber}号座位</h4>
                    <p>日期: ${res.date}</p>
                    <p>时间: ${res.startTime} - ${res.endTime}</p>
                    <p>状态: <span style="color: ${getResStatusColor(res.status)}">${getResStatusText(res.status)}</span></p>
                    ${res.remark ? `<p>备注: ${res.remark}</p>` : ''}
                    ${res.status === 'PENDING' || res.status === 'CONFIRMED' ? `
                        <div class="reservation-actions">
                            <button class="btn btn-small btn-danger" onclick="cancelReservation(${res.id})">取消预约</button>
                        </div>
                    ` : ''}
                </div>
            `).join('')}
        </div>
    `;
}

// 取消预约
async function cancelReservation(reservationId) {
    if (!confirm('确定要取消这个预约吗？')) return;
    try {
        await ReservationAPI.cancel(currentUser.id, reservationId);
        showToast('预约已取消', 'success');
        showMyReservations();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 工具函数
function getStatusText(status) {
    const map = {
        'OPEN': '开放',
        'CLOSED': '关闭',
        'MAINTENANCE': '维护中'
    };
    return map[status] || status;
}

function getSeatStatusText(status) {
    const map = {
        'AVAILABLE': '可预约',
        'OCCUPIED': '已占用',
        'MAINTENANCE': '维护中'
    };
    return map[status] || status;
}

function getResStatusText(status) {
    const map = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'CANCELLED': '已取消',
        'COMPLETED': '已完成'
    };
    return map[status] || status;
}

function getResStatusColor(status) {
    const map = {
        'PENDING': '#ffc107',
        'CONFIRMED': '#28a745',
        'CANCELLED': '#6c757d',
        'COMPLETED': '#17a2b8'
    };
    return map[status] || '#333';
}

// Toast提示
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => toast.classList.add('show'), 10);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ========== 管理员预约管理功能 ==========

// 显示管理员预约管理页面
async function showAdminReservationManagement() {
    if (!isAdmin()) {
        showToast('只有管理员可以访问此功能', 'error');
        return;
    }
    
    document.getElementById('study-rooms-section').style.display = 'none';
    document.getElementById('seats-section').style.display = 'none';
    document.getElementById('reservations-section').style.display = 'none';
    document.getElementById('admin-reservations-section').style.display = 'block';
    
    await loadAdminReservations();
}

// 加载所有预约数据
async function loadAdminReservations(status = 'all') {
    try {
        let reservations;
        if (status === 'all') {
            reservations = await ReservationAPI.adminGetAll();
        } else {
            reservations = await ReservationAPI.adminGetByStatus(status);
        }
        renderAdminReservations(reservations);
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 渲染管理员预约列表
function renderAdminReservations(reservations) {
    const container = document.getElementById('admin-reservations-list');
    if (!reservations || reservations.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: #666; padding: 40px;">暂无预约记录</p>';
        return;
    }
    
    container.innerHTML = reservations.map(res => `
        <div class="reservation-card" style="
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 15px;
            background: #fff;
        ">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div style="flex: 1;">
                    <h4 style="margin: 0 0 10px 0;">${res.studyRoomName} - ${res.seatNumber}号座位</h4>
                    <p style="margin: 5px 0; color: #666;">
                        <strong>预约用户:</strong> ${res.userName} (ID: ${res.userId})
                    </p>
                    <p style="margin: 5px 0; color: #666;">
                        <strong>预约时间:</strong> ${res.date} ${res.startTime}-${res.endTime}
                    </p>
                    <p style="margin: 5px 0; color: #666;">
                        <strong>状态:</strong> 
                        <span style="color: ${getResStatusColor(res.status)}; font-weight: bold;">
                            ${getResStatusText(res.status)}
                        </span>
                    </p>
                    ${res.remark ? `<p style="margin: 5px 0; color: #666;"><strong>备注:</strong> ${res.remark}</p>` : ''}
                </div>
                <div style="display: flex; flex-direction: column; gap: 8px;">
                    ${res.status !== 'CANCELLED' && res.status !== 'COMPLETED' ? `
                        <button class="btn btn-small btn-warning" onclick="editReservation(${res.id})">修改</button>
                        <button class="btn btn-small btn-danger" onclick="cancelReservationAdmin(${res.id})">取消</button>
                    ` : ''}
                    <button class="btn btn-small btn-danger" onclick="deleteReservationAdmin(${res.id})">删除</button>
                </div>
            </div>
        </div>
    `).join('');
}

// 筛选预约状态
function filterReservationsByStatus() {
    const status = document.getElementById('reservation-status-filter').value;
    loadAdminReservations(status);
}

// 管理员取消预约
async function cancelReservationAdmin(reservationId) {
    if (!confirm('确定要取消这个预约吗？')) return;
    
    try {
        await ReservationAPI.adminCancel(reservationId);
        showToast('预约已取消', 'success');
        filterReservationsByStatus();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 管理员删除预约
async function deleteReservationAdmin(reservationId) {
    if (!confirm('确定要删除这个预约记录吗？此操作不可恢复！')) return;
    
    try {
        await ReservationAPI.adminDelete(reservationId);
        showToast('预约记录已删除', 'success');
        filterReservationsByStatus();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 编辑预约
async function editReservation(reservationId) {
    try {
        const reservation = await ReservationAPI.getById(reservationId);
        const rooms = await StudyRoomAPI.getAll();
        
        // 获取当前座位的可用时间段
        const availableSlots = await ReservationAPI.getAvailableTimeSlots(reservation.seatId, reservation.date);
        
        // 生成自习室选项
        const roomOptions = rooms.map(room => 
            `<option value="${room.id}" ${room.id === reservation.studyRoomId ? 'selected' : ''}>${room.name}</option>`
        ).join('');
        
        // 生成时间段选项（包含当前预约的时间段）
        const currentSlotIndex = TIME_SLOTS.findIndex(s => 
            s.start === reservation.startTime && s.end === reservation.endTime
        );
        
        const timeSlotOptions = TIME_SLOTS.map(slot => {
            const isAvailable = availableSlots.some(s => s.index === slot.index) || slot.index === currentSlotIndex;
            return `
                <option value="${slot.index}" ${slot.index === currentSlotIndex ? 'selected' : ''} ${!isAvailable ? 'disabled' : ''}>
                    ${slot.label} ${!isAvailable ? '[已预约]' : ''}
                </option>
            `;
        }).join('');
        
        document.getElementById('modal-content').innerHTML = `
            <h2>修改预约</h2>
            <form id="edit-reservation-form">
                <input type="hidden" id="edit-res-id" value="${reservation.id}">
                <div class="form-group">
                    <label>预约用户</label>
                    <input type="text" value="${reservation.userName}" disabled>
                </div>
                <div class="form-group">
                    <label>自习室</label>
                    <select id="edit-room-id" required onchange="loadSeatsForEdit()">
                        ${roomOptions}
                    </select>
                </div>
                <div class="form-group">
                    <label>座位</label>
                    <select id="edit-seat-id" required>
                        <!-- 动态加载 -->
                    </select>
                </div>
                <div class="form-group">
                    <label>预约日期</label>
                    <input type="date" id="edit-date" value="${reservation.date}" required onchange="updateEditTimeSlots()">
                </div>
                <div class="form-group">
                    <label>时间段</label>
                    <select id="edit-time-slot" required>
                        ${timeSlotOptions}
                    </select>
                </div>
                <div class="form-group">
                    <label>备注</label>
                    <textarea id="edit-remark" rows="2">${reservation.remark || ''}</textarea>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">保存修改</button>
                    <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
                </div>
            </form>
        `;
        document.getElementById('modal-overlay').classList.add('active');
        
        // 加载座位
        await loadSeatsForEdit(reservation.seatId);
        
        document.getElementById('edit-reservation-form').addEventListener('submit', handleEditReservation);
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 加载座位选项（编辑用）
async function loadSeatsForEdit(selectedSeatId = null) {
    const roomId = document.getElementById('edit-room-id').value;
    try {
        const seats = await SeatAPI.getByRoom(roomId);
        const seatSelect = document.getElementById('edit-seat-id');
        seatSelect.innerHTML = seats.map(seat => 
            `<option value="${seat.id}" ${seat.id === selectedSeatId ? 'selected' : ''}>${seat.seatNumber}号座位</option>`
        ).join('');
    } catch (error) {
        showToast('加载座位失败', 'error');
    }
}

// 更新编辑时的时间段选项
async function updateEditTimeSlots() {
    const seatId = document.getElementById('edit-seat-id').value;
    const date = document.getElementById('edit-date').value;
    if (!seatId || !date) return;
    
    try {
        const availableSlots = await ReservationAPI.getAvailableTimeSlots(seatId, date);
        const timeSlotSelect = document.getElementById('edit-time-slot');
        const currentValue = timeSlotSelect.value;
        
        timeSlotSelect.innerHTML = TIME_SLOTS.map(slot => {
            const isAvailable = availableSlots.some(s => s.index === slot.index);
            return `
                <option value="${slot.index}" ${slot.index === parseInt(currentValue) ? 'selected' : ''} ${!isAvailable ? 'disabled' : ''}>
                    ${slot.label} ${!isAvailable ? '[已预约]' : ''}
                </option>
            `;
        }).join('');
    } catch (error) {
        console.log('更新时间段失败', error);
    }
}

// 处理编辑预约提交
async function handleEditReservation(e) {
    e.preventDefault();
    try {
        const reservationId = document.getElementById('edit-res-id').value;
        const reservationData = {
            seatId: parseInt(document.getElementById('edit-seat-id').value),
            date: document.getElementById('edit-date').value,
            timeSlot: parseInt(document.getElementById('edit-time-slot').value),
            remark: document.getElementById('edit-remark').value
        };
        
        await ReservationAPI.adminUpdate(reservationId, reservationData);
        closeModal();
        showToast('预约修改成功', 'success');
        filterReservationsByStatus();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 返回主页
function backToHome() {
    document.getElementById('admin-reservations-section').style.display = 'none';
    document.getElementById('study-rooms-section').style.display = 'block';
    loadStudyRooms();
}

// 创建测试数据
async function createTestData() {
    try {
        const rooms = await StudyRoomAPI.getAll();
        if (rooms && rooms.length === 0) {
            try {
                await UserAPI.registerAdmin({
                    username: 'admin',
                    password: 'admin123',
                    name: '管理员',
                    email: 'admin@example.com',
                    phone: '13800138000'
                });
            } catch (e) {
                console.log('管理员账户已存在', e);
            }
            
            const adminUser = await UserAPI.login('admin', 'admin123');
            
            await StudyRoomAPI.create(adminUser.id, {
                name: '图书馆自习室',
                location: '图书馆三楼',
                description: '安静的学习环境，配备空调和饮水机',
                totalSeats: 30,
                openTime: '08:00',
                closeTime: '22:00',
                status: 'OPEN'
            });
            await StudyRoomAPI.create(adminUser.id, {
                name: '教学楼A座自习室',
                location: 'A座101室',
                description: '宽敞明亮，适合小组讨论',
                totalSeats: 50,
                openTime: '07:00',
                closeTime: '23:00',
                status: 'OPEN'
            });
            await StudyRoomAPI.create(adminUser.id, {
                name: '教学楼B座自习室',
                location: 'B座205室',
                description: '空调自习室，环境舒适',
                totalSeats: 40,
                openTime: '08:00',
                closeTime: '22:00',
                status: 'OPEN'
            });
            loadStudyRooms();
        }
    } catch (e) {
        console.log('创建测试数据失败', e);
    }
}
