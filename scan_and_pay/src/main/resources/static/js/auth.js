// Authentication JavaScript
class AuthManager {
    constructor() {
        this.currentStep = 1;
        this.otpSent = false;
        this.setupEventListeners();
    }

    setupEventListeners() {
        // Login form
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }

        // Register form
        const registerForm = document.getElementById('registerForm');
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => this.handleRegister(e));
        }

        // OTP verification
        const verifyForm = document.getElementById('verifyForm');
        if (verifyForm) {
            verifyForm.addEventListener('submit', (e) => this.handleVerifyOTP(e));
        }

        // Role selection
        this.setupRoleSelection();

        // OTP input auto-focus
        this.setupOTPInput();
    }

    setupRoleSelection() {
        const roleOptions = document.querySelectorAll('.role-option');
        roleOptions.forEach(option => {
            option.addEventListener('click', () => {
                roleOptions.forEach(opt => opt.classList.remove('selected'));
                option.classList.add('selected');
                document.getElementById('role').value = option.dataset.role;
            });
        });
    }

    setupOTPInput() {
        const otpInputs = document.querySelectorAll('.otp-input');
        otpInputs.forEach((input, index) => {
            input.addEventListener('input', (e) => {
                if (e.target.value.length === 1) {
                    if (index < otpInputs.length - 1) {
                        otpInputs[index + 1].focus();
                    }
                }
            });

            input.addEventListener('keydown', (e) => {
                if (e.key === 'Backspace' && e.target.value === '') {
                    if (index > 0) {
                        otpInputs[index - 1].focus();
                    }
                }
            });
        });
    }

    async handleLogin(e) {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData);

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (response.ok) {
                if (result.requiresOtp) {
                    this.showOTPStep(result.userId);
                } else {
                    this.completeLogin(result);
                }
            } else {
                Utils.showNotification(result.message || 'Login failed', 'error');
            }
        } catch (error) {
            console.error('Login error:', error);
            Utils.showNotification('Network error. Please try again.', 'error');
        }
    }

    async handleRegister(e) {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData);

        // Validate passwords match
        if (data.password !== data.confirmPassword) {
            Utils.showNotification('Passwords do not match', 'error');
            return;
        }

        try {
            const response = await fetch('/api/users/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (response.ok) {
                Utils.showNotification('Registration successful! Please check your email for verification.', 'success');
                setTimeout(() => {
                    window.location.href = '/templates/login.html';
                }, 2000);
            } else {
                Utils.showNotification(result.message || 'Registration failed', 'error');
            }
        } catch (error) {
            console.error('Registration error:', error);
            Utils.showNotification('Network error. Please try again.', 'error');
        }
    }

    async handleVerifyOTP(e) {
        e.preventDefault();
        const otpInputs = document.querySelectorAll('.otp-input');
        const otpCode = Array.from(otpInputs).map(input => input.value).join('');

        if (otpCode.length !== 6) {
            Utils.showNotification('Please enter the complete OTP code', 'error');
            return;
        }

        const email = document.getElementById('loginEmail').value;

        try {
            const response = await fetch('/api/auth/verify-login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    email: email,
                    otpCode: otpCode
                })
            });

            const result = await response.json();

            if (response.ok) {
                this.completeLogin(result);
            } else {
                Utils.showNotification(result.message || 'Invalid OTP', 'error');
            }
        } catch (error) {
            console.error('OTP verification error:', error);
            Utils.showNotification('Network error. Please try again.', 'error');
        }
    }

    showOTPStep(userId) {
        document.getElementById('loginStep').classList.add('hidden');
        document.getElementById('otpStep').classList.remove('hidden');
        this.otpSent = true;
        this.startResendTimer();
    }

    startResendTimer() {
        let timeLeft = 60;
        const resendBtn = document.getElementById('resendOtpBtn');
        const timerElement = document.getElementById('resendTimer');

        const timer = setInterval(() => {
            timeLeft--;
            timerElement.textContent = timeLeft;

            if (timeLeft <= 0) {
                clearInterval(timer);
                resendBtn.disabled = false;
                timerElement.textContent = '';
            }
        }, 1000);
    }

    async resendOTP() {
        const email = document.getElementById('loginEmail').value;

        try {
            const response = await fetch('/api/otp/send-login?email=' + encodeURIComponent(email), {
                method: 'POST'
            });

            if (response.ok) {
                Utils.showNotification('OTP sent successfully', 'success');
                this.startResendTimer();
            } else {
                Utils.showNotification('Failed to resend OTP', 'error');
            }
        } catch (error) {
            console.error('Resend OTP error:', error);
            Utils.showNotification('Network error. Please try again.', 'error');
        }
    }

    completeLogin(result) {
        localStorage.setItem('scanpay_token', result.token);
        Utils.showNotification('Login successful!', 'success');

        // Redirect based on user role
        setTimeout(() => {
            if (result.user.role === 'ADMIN') {
                window.location.href = '/templates/admin-dashboard.html';
            } else if (result.user.role === 'MERCHANT') {
                window.location.href = '/templates/merchant-dashboard.html';
            } else {
                window.location.href = '/templates/dashboard.html';
            }
        }, 1000);
    }
}

// Initialize auth manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.authManager = new AuthManager();
});