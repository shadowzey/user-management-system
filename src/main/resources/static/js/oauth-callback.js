/**
 * 处理OAuth2登录回调
 * 从URL参数中获取token并存储到cookie
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('OAuth2回调页面加载');
    console.log('当前URL:', window.location.href);
    
    // 从URL获取token参数
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    console.log('URL中的token:', token);
    
    // 检查Cookie中是否有token
    const cookies = document.cookie.split(';');
    let cookieToken = null;
    for (let i = 0; i < cookies.length; i++) {
        const cookie = cookies[i].trim();
        if (cookie.startsWith('jwt=')) {
            cookieToken = cookie.substring(4);
            break;
        }
    }
    console.log('Cookie中的token:', cookieToken);
    
    // 优先使用URL中的token，如果没有则使用Cookie中的
    const finalToken = token || cookieToken;
    
    if (finalToken) {
        console.log('已有token，开始获取用户信息');
        
        // 清除URL中的token参数
        const url = new URL(window.location.href);
        url.searchParams.delete('token');
        window.history.replaceState({}, document.title, url.toString());
        
        // 获取用户信息
        fetchUserInfo(finalToken);
    } else {
        console.error('未找到有效的token');
        document.getElementById('error-message').textContent = '登录失败：未找到有效的令牌';
        document.getElementById('error-message').classList.remove('d-none');
    }
});

/**
 * 获取用户信息并存储到cookie
 * @param {string} token - JWT令牌
 */
async function fetchUserInfo(token) {
    console.log('开始获取用户信息，token:', token);
    try {
        // 检查cookie中的token
        const cookies = document.cookie.split(';');
        let cookieToken = null;
        for (let i = 0; i < cookies.length; i++) {
            const cookie = cookies[i].trim();
            if (cookie.startsWith('jwt=')) {
                cookieToken = cookie.substring(4);
                break;
            }
        }
        console.log('Cookie中的token:', cookieToken);
        
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
            
            // 将用户数据存储在cookie中
            const date = new Date();
            date.setTime(date.getTime() + (7 * 24 * 60 * 60 * 1000)); // 7天过期
            const expires = "expires=" + date.toUTCString();
            document.cookie = "user=" + JSON.stringify(userData) + ";" + expires + ";path=/";
            console.log('用户数据已存储到cookie');
            
            // 显示成功消息
            document.querySelector('.spinner-border').style.display = 'none';
            document.querySelector('p.mt-3').textContent = '登录成功！正在跳转到个人资料页面...';
            
            // 延时一秒后跳转，让用户看到成功消息
            setTimeout(() => {
                // 重定向到个人资料页面
                window.location.href = '/profile';
            }, 1000);
        } else {
            // Token无效或过期
            console.error('获取用户信息失败，状态码:', response.status);
            document.querySelector('.spinner-border').style.display = 'none';
            document.getElementById('error-message').textContent = '登录失败：无法获取用户信息';
            document.getElementById('error-message').classList.remove('d-none');
        }
    } catch (error) {
        console.error('获取用户信息时出错:', error);
        document.querySelector('.spinner-border').style.display = 'none';
        document.getElementById('error-message').textContent = '登录失败：' + error.message;
        document.getElementById('error-message').classList.remove('d-none');
    }
}
