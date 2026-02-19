// ===== BPO Helpdesk - API Helper =====
const API_BASE = '';

const api = {
    getToken() {
        return localStorage.getItem('token');
    },

    getUser() {
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    },

    saveAuth(data) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({
            username: data.username,
            role: data.role,
            fullName: data.fullName
        }));
    },

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login.html';
    },

    isLoggedIn() {
        return !!this.getToken();
    },

    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = '/login.html';
            return false;
        }
        return true;
    },

    requireRole(roles) {
        const user = this.getUser();
        if (!user || !roles.includes(user.role)) {
            window.location.href = '/dashboard.html';
            return false;
        }
        return true;
    },

    async request(method, url, data = null) {
        const headers = { 'Content-Type': 'application/json' };
        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = { method, headers };
        if (data) {
            config.body = JSON.stringify(data);
        }

        const response = await fetch(API_BASE + url, config);
        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || 'Request failed');
        }

        return result;
    },

    get(url) { return this.request('GET', url); },
    post(url, data) { return this.request('POST', url, data); },
    put(url, data) { return this.request('PUT', url, data); }
};

// ===== UI Helpers =====
function showAlert(containerId, message, type = 'danger') {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = `
            <div class="alert alert-${type} alert-custom alert-dismissible fade show" role="alert">
                <i class="bi bi-${type === 'success' ? 'check-circle' : 'exclamation-triangle'}-fill me-2"></i>
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>`;
        setTimeout(() => { container.innerHTML = ''; }, 5000);
    }
}

function statusBadge(status) {
    return `<span class="badge-status badge-${status}">${status}</span>`;
}

function priorityBadge(priority) {
    return `<span class="badge-status badge-${priority}">${priority}</span>`;
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    return dateStr;
}

// Update navbar based on login state
function updateNavbar() {
    const user = api.getUser();
    const navRight = document.getElementById('navRight');
    if (!navRight) return;

    if (user) {
        navRight.innerHTML = `
            <li class="nav-item"><a class="nav-link" href="/dashboard.html">
                <i class="bi bi-grid-fill me-1"></i>Dashboard</a></li>
            ${(user.role === 'ADMIN' || user.role === 'AGENT') ?
                '<li class="nav-item"><a class="nav-link" href="/admin.html"><i class="bi bi-shield-fill me-1"></i>Admin Panel</a></li>' : ''}
            <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                    <i class="bi bi-person-circle me-1"></i>${user.fullName}
                </a>
                <ul class="dropdown-menu dropdown-menu-end">
                    <li><span class="dropdown-item-text text-muted small">${user.role}</span></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item" href="#" onclick="api.logout()">
                        <i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
                </ul>
            </li>`;
    } else {
        navRight.innerHTML = `
            <li class="nav-item"><a class="nav-link" href="/login.html">
                <i class="bi bi-box-arrow-in-right me-1"></i>Login</a></li>
            <li class="nav-item"><a class="nav-link" href="/register.html">
                <i class="bi bi-person-plus me-1"></i>Register</a></li>`;
    }
}

document.addEventListener('DOMContentLoaded', updateNavbar);
