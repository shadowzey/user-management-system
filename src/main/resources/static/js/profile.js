/**
 * 个人资料页面脚本
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('个人资料页面加载');
    
    // 检查登录状态
    if (!checkAuth()) {
        console.log('用户未登录，重定向到登录页面');
        window.location.href = '/login';
        return;
    }
    
    console.log('用户已登录，token:', localStorage.getItem('token'));
    
    // 加载用户信息
    loadUserProfile();
    
    // 绑定退出按钮事件
    document.getElementById('logout-btn').addEventListener('click', function(e) {
        e.preventDefault();
        logout();
    });
});

/**
 * 加载用户个人资料
 */
async function loadUserProfile() {
    try {
        const token = localStorage.getItem('token');
        console.log('使用token获取用户信息:', token);
        
        const response = await fetch('/api/user/profile', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        console.log('获取用户信息响应状态:', response.status);
        
        if (response.ok) {
            const userData = await response.json();
            console.log('获取到的用户数据:', userData);
            
            // 更新用户信息显示
            document.getElementById('username').textContent = userData.username || '未设置';
            document.getElementById('email').textContent = userData.email || '未设置';
            document.getElementById('real-name').textContent = userData.realName || '未设置';
            document.getElementById('phone-number').textContent = userData.phoneNumber || '未设置';
            document.getElementById('address').textContent = userData.address || '未设置';
            document.getElementById('city').textContent = userData.city || '未设置';
            document.getElementById('postal-code').textContent = userData.postalCode || '未设置';
            document.getElementById('country').textContent = userData.country || '未设置';
            document.getElementById('bio').textContent = userData.bio || '未设置';
            
            // 如果用户是通过OAuth2登录的，显示相关信息
            if (userData.oauthProvider) {
                document.getElementById('oauth-info').classList.remove('d-none');
                document.getElementById('oauth-provider').textContent = userData.oauthProvider;
                document.getElementById('oauth-id').textContent = userData.oauthId;
            }
            
            // 存储用户数据
            localStorage.setItem('user', JSON.stringify(userData));
        } else {
            // Token无效
            console.error('获取用户信息失败，状态码:', response.status);
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
    } catch (error) {
        console.error('加载用户资料时出错:', error);
        alert('加载用户资料失败: ' + error.message);
    }
}
