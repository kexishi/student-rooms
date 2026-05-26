// API服务层
const API_BASE = '/api';

async function request(url, options = {}) {
    const response = await fetch(`${API_BASE}${url}`, {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        ...options
    });
    
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || '请求失败');
    }
    return data;
}

// 用户API
const UserAPI = {
    async register(userData) {
        const result = await request('/users/register', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
        return result.data;
    },

    async registerAdmin(userData) {
        const result = await request('/users/register/admin', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
        return result.data;
    },

    async login(username, password) {
        const result = await request('/users/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        return result.data;
    },

    async getUser(id) {
        const result = await request(`/users/${id}`);
        return result.data;
    }
};

// 自习室API
const StudyRoomAPI = {
    async getAll() {
        const result = await request('/study-rooms');
        return result.data;
    },

    async getById(id) {
        const result = await request(`/study-rooms/${id}`);
        return result.data;
    },

    async create(userId, roomData) {
        const result = await request(`/study-rooms/user/${userId}`, {
            method: 'POST',
            body: JSON.stringify(roomData)
        });
        return result.data;
    },

    async update(id, roomData) {
        // 使用当前登录用户ID
        const userId = JSON.parse(localStorage.getItem('currentUser'))?.id;
        const result = await request(`/study-rooms/user/${userId}/${id}`, {
            method: 'PUT',
            body: JSON.stringify(roomData)
        });
        return result.data;
    },

    async delete(id) {
        // 使用当前登录用户ID
        const userId = JSON.parse(localStorage.getItem('currentUser'))?.id;
        const result = await request(`/study-rooms/user/${userId}/${id}`, {
            method: 'DELETE'
        });
        return result.data;
    }
};

// 座位API
const SeatAPI = {
    async getByRoom(roomId) {
        const result = await request(`/seats/study-room/${roomId}`);
        return result.data;
    },

    async getAvailableByRoom(roomId) {
        const result = await request(`/seats/study-room/${roomId}/available`);
        return result.data;
    }
};

// 预约API
const ReservationAPI = {
    async create(userId, reservationData) {
        const result = await request(`/reservations/user/${userId}`, {
            method: 'POST',
            body: JSON.stringify(reservationData)
        });
        return result.data;
    },

    async getUserReservations(userId) {
        const result = await request(`/reservations/user/${userId}`);
        return result.data;
    },

    async cancel(userId, reservationId) {
        const result = await request(`/reservations/user/${userId}/reservation/${reservationId}`, {
            method: 'DELETE'
        });
        return result.data;
    },

    async getBySeatAndDate(seatId, date) {
        const result = await request(`/reservations/seat/${seatId}/date/${date}`);
        return result.data;
    },

    async getById(reservationId) {
        const result = await request(`/reservations/${reservationId}`);
        return result.data;
    },

    async getAvailableTimeSlots(seatId, date) {
        const result = await request(`/reservations/seat/${seatId}/date/${date}/available-slots`);
        return result.data;
    },

    // ========== 管理员接口 ==========
    async adminGetAll() {
        const result = await request('/reservations/admin/all');
        return result.data;
    },

    async adminGetByStatus(status) {
        const result = await request(`/reservations/admin/status/${status}`);
        return result.data;
    },

    async adminCancel(reservationId) {
        const result = await request(`/reservations/admin/${reservationId}/cancel`, {
            method: 'POST'
        });
        return result.data;
    },

    async adminUpdate(reservationId, reservationData) {
        const result = await request(`/reservations/admin/${reservationId}`, {
            method: 'PUT',
            body: JSON.stringify(reservationData)
        });
        return result.data;
    },

    async adminDelete(reservationId) {
        const result = await request(`/reservations/admin/${reservationId}`, {
            method: 'DELETE'
        });
        return result.data;
    }
};