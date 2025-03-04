/**
 * 管理后台通用JavaScript
 */

// 格式化日期时间函数
function formatDateTime(timestamp) {
    if (!timestamp) return '-';
    
    // 处理ISO格式的时间字符串
    const date = new Date(timestamp);
    if (isNaN(date.getTime())) {
        console.error('无效的时间格式:', timestamp);
        return timestamp; // 返回原始值
    }
    
    // 格式化为本地时间格式
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    });
}

// 检查管理员权限
async function checkAdminAuth() {
    if (!checkAuth()) {
        window.location.href = '/login';
        return false;
    }
    
    // 临时移除管理员权限检查，以便调试
    /*
    if (!isAdmin()) {
        alert('您没有管理员权限');
        window.location.href = '/';
        return false;
    }
    */
    
    return true;
}

// 获取所有用户（分页）
async function fetchUsers(page = 0, size = 10, sortBy = 'id', direction = 'asc') {
    const token = localStorage.getItem('token');
    if (!token) return null;
    
    try {
        const response = await fetch(`/api/admin/users?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            console.log('获取到的用户数据:', data);
            
            // 检查用户状态
            if (data && data.users) {
                data.users.forEach(user => {
                    console.log(`用户 ${user.username} 状态: ${user.enabled}`);
                });
            }
            
            return data;
        } else {
            console.error('获取用户列表失败');
            return null;
        }
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

// 获取用户详情
async function fetchUserDetails(userId) {
    const token = localStorage.getItem('token');
    if (!token) return null;
    
    try {
        const response = await fetch(`/api/admin/users/${userId}`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        if (response.ok) {
            return await response.json();
        } else {
            console.error('获取用户详情失败');
            return null;
        }
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

// 更新用户信息
async function updateUser(userId, userData) {
    const token = localStorage.getItem('token');
    if (!token) return false;
    
    try {
        const response = await fetch(`/api/admin/users/${userId}`, {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });
        
        if (response.ok) {
            return true;
        } else {
            console.error('更新用户信息失败');
            return false;
        }
    } catch (error) {
        console.error('Error:', error);
        return false;
    }
}

// 更新用户状态（启用/禁用）
async function updateUserStatus(userId, enabled) {
    const token = localStorage.getItem('token');
    if (!token) return false;
    
    try {
        const response = await fetch(`/api/admin/users/${userId}/status`, {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ enabled })
        });
        
        if (response.ok) {
            return true;
        } else {
            console.error('更新用户状态失败');
            return false;
        }
    } catch (error) {
        console.error('Error:', error);
        return false;
    }
}

// 删除用户
async function deleteUser(userId) {
    const token = localStorage.getItem('token');
    if (!token) return false;
    
    try {
        const response = await fetch(`/api/admin/users/${userId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        if (response.ok) {
            return true;
        } else {
            console.error('删除用户失败');
            return false;
        }
    } catch (error) {
        console.error('Error:', error);
        return false;
    }
}

// 获取用户日志
async function fetchUserLogs(userId = null, username = null, action = null, status = null, days = null) {
    const token = localStorage.getItem('token');
    if (!token) return null;
    
    // 如果是为了仪表盘最近活动，使用分页方式获取
    if (!userId && days) {
        return fetchLogs(0, 10, 'timestamp', 'desc', '', '', '', days);
    }
    
    // 使用专门的用户日志端点
    let url = userId ? `/api/admin/logs/user/${userId}` : '/api/admin/logs?';
    
    // 如果不是用户特定的日志，则添加其他查询参数
    if (!userId) {
        if (username) url += `username=${username}&`;
        if (action) url += `action=${action}&`;
        if (status) url += `status=${status}&`;
        if (days) url += `days=${days}&`;
    }
    
    console.log('获取用户日志URL:', url);
    
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            console.log('获取到的用户日志数据:', data);
            return data;
        } else {
            console.error('获取用户日志失败, 状态码:', response.status);
            return null;
        }
    } catch (error) {
        console.error('获取用户日志出错:', error);
        return null;
    }
}

// 获取所有日志（分页）
async function fetchLogs(page = 0, size = 20, sortBy = 'timestamp', direction = 'desc', search = '', action = '', status = '', days = null) {
    const token = localStorage.getItem('token');
    if (!token) return null;
    
    try {
        let url = `/api/admin/logs?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
        
        if (search) {
            url += `&search=${encodeURIComponent(search)}`;
        }
        
        if (action) {
            url += `&action=${encodeURIComponent(action)}`;
        }
        
        if (status) {
            url += `&status=${encodeURIComponent(status)}`;
        }
        
        if (days) {
            url += `&days=${days}`;
        }
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        if (response.ok) {
            return await response.json();
        } else {
            console.error('获取日志列表失败');
            return null;
        }
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

// 格式化日期时间
function formatDateTime(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 检查管理员权限
    checkAdminAuth();
    
    // 高亮当前页面对应的侧边栏菜单
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.admin-sidebar .nav-link');
    
    navLinks.forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
});
