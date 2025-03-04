/**
 * 从 Cookie 中获取指定名称的值
 * @param {string} name - Cookie 名称
 * @returns {string|null} Cookie 值或 null
 */
function getCookie(name) {
    console.log('Getting cookie:', name);
    console.log('All cookies:', document.cookie);
    
    if (!document.cookie) {
        console.log('No cookies found');
        return null;
    }
    
    const cookies = document.cookie.split(';');
    console.log('Split cookies:', cookies);
    
    for (let i = 0; i < cookies.length; i++) {
        const cookie = cookies[i].trim();
        console.log('Checking cookie:', cookie);
        
        if (cookie.startsWith(name + '=')) {
            const value = cookie.substring(name.length + 1);
            console.log('Found cookie value for', name, ':', value.substring(0, Math.min(20, value.length)) + '...');
            return value;
        }
    }
    
    console.log('Cookie not found:', name);
    return null;
}

/**
 * 设置 Cookie
 * @param {string} name - Cookie 名称
 * @param {string} value - Cookie 值
 * @param {number} days - 过期天数
 */
function setCookie(name, value, days) {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    const expires = "expires=" + date.toUTCString();
    document.cookie = name + "=" + value + ";" + expires + ";path=/";
}

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
function checkAuth() {
    // 从 Cookie 中获取 token
    const token = getCookie('jwt');
    
    // 如果有token，尝试获取用户信息
    if (token) {
        console.log('从 Cookie 中获取到 token');
        
        // 如果没有用户信息，尝试获取
        if (!getCookie('user')) {
            fetchUserInfoForAuth(token);
        }
    }
    
    return !!token;
}

/**
 * 获取用户信息并存入 Cookie
 * @param {string} token - JWT 令牌
 */
async function fetchUserInfoForAuth(token) {
    try {
        const response = await fetch('/api/user/profile', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        if (response.ok) {
            const userData = await response.json();
            setCookie('user', JSON.stringify(userData), 7); // 存储7天
            console.log('用户信息已存入 Cookie');
        } else {
            console.error('获取用户信息失败，状态码:', response.status);
            document.cookie = 'jwt=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        }
    } catch (error) {
        console.error('获取用户信息时出错:', error);
        document.cookie = 'jwt=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    }
}

/**
 * 退出登录
 */
function logout() {
    // 清除所有相关Cookie
    document.cookie = 'jwt=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    document.cookie = 'user=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    window.location.href = '/login';
}

/**
 * 获取当前登录用户信息
 * @returns {Promise} 包含用户信息的Promise
 */
async function getCurrentUser() {
    const token = getCookie('jwt');
    if (!token) {
        return null;
    }
    
    // 先尝试从 Cookie 中获取用户信息
    const userStr = getCookie('user');
    if (userStr) {
        try {
            return JSON.parse(userStr);
        } catch (e) {
            console.error('Error parsing user data from cookie:', e);
        }
    }
    
    // 如果 Cookie 中没有用户信息，从服务器获取
    try {
        const response = await fetch('/api/user/profile', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        if (response.ok) {
            const userData = await response.json();
            // 将用户数据存储在Cookie中
            setCookie('user', JSON.stringify(userData), 7);
            return userData;
        } else {
            // Token无效或过期
            document.cookie = 'jwt=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
            document.cookie = 'user=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
            return null;
        }
    } catch (error) {
        console.error('Error fetching user info:', error);
        return null;
    }
}

/**
 * 检查当前用户是否为管理员
 * @returns {boolean} 是否为管理员
 */
function isAdmin() {
    const userStr = getCookie('user');
    if (!userStr) {
        return false;
    }
    
    try {
        const user = JSON.parse(userStr);
        return user.roles && user.roles.includes('ROLE_ADMIN');
    } catch (error) {
        console.error('Error checking admin status:', error);
        return false;
    }
}
