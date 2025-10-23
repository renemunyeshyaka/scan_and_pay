class ResetPasswordManager {
    constructor() {
        this.setupEventListeners();
        this.setupOTPInput();
        this.startResendTimer();
    }

    setupEventListeners() {
        const form = document.getElementById('resetPasswordForm');
        if (form) {
            form.addEventListener('submit', (e) => this.handleResetPassword(e));
        }

        // Password strength and match validation
        document.getElementById('newPassword').addEventListener('input', (e) => {
            this.checkPasswordStrength(e.target.value);
            this.checkPasswordMatch();
        });

        document.getElementById('confirmPassword').addEventListener('input', () => {
            this.checkPasswordMatch();
        });

        // Resend OTP
        document.getElementById('resendOtpBtn').addEventListener('click', () => {
            this.resendOTP();
        });
    }

    setupOTPInput() {
        const otpInputs = document.querySelectorAll('.otp-input');
        const hiddenOtpInput = document.getElementById('otpCode');

        otpInputs.forEach((input, index) => {
            input.addEventListener('input', (e) => {
                if (e.target.value.length === 1) {
                    if (index < otpInputs.length - 1) {
                        otpInputs[index + 1].focus();
                    }
                }
                this.updateHiddenOTP();
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

    updateHiddenOTP() {
        const otpInputs = document.querySelectorAll('.otp-input');
        const otpCode = Array.from(otpInputs).map(input => input.value).join('');
        document.getElementById('otpCode').value = otpCode;
        this.validateForm();
    }

    checkPasswordStrength(password) {
        const strengthBar = document.getElementById('strengthBar');
        const strengthText = document.getElementById('strengthText');
        
        let strength = 0;
        let text = 'Very Weak';
        let color = '#ef4444';

        if (password.length >= 6) strength += 25;
        if (password.match(/[a-z]/) && password.match(/[A-Z]/)) strength += 25;
        if (password.match(/\d/)) strength += 25;
        if (password.match(/[^a-zA-Z\d]/)) strength += 25;

        if (strength >= 75) {
            text = 'Strong';
            color = '#10b981';
        } else if (strength >= 50) {
            text = 'Good';
            color = '#f59e0b';
        } else if (strength >= 25) {
            text = 'Weak';
            color = '#f59e0b';
        }

        strengthBar.style.width = strength + '%';
        strengthBar.style.backgroundColor = color;
        strengthText.textContent = text;
        strengthText.style.color = color;
    }

    checkPasswordMatch() {
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const matchElement = document.getElementById('passwordMatch');
        const resetBtn = document.getElementById('resetBtn');

        if (confirmPassword && newPassword === confirmPassword) {
            matchElement.style.display = 'flex';
            resetBtn.disabled = false;
        } else {
            matchElement.style.display = 'none';
            resetBtn.disabled = true;
        }
    }

    validateForm() {
        const otpCode = document.getElementById('otpCode').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const resetBtn = document.getElementById('resetBtn');

        resetBtn.disabled = !(otpCode.length === 6 && newPassword && newPassword === confirmPassword);
    }

    async handleResetPassword(e) {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData);
        const resetBtn = document.getElementById('resetBtn');

        try {
            resetBtn.disabled = true;
            resetBtn.textContent = 'Resetting...';

            const response = await fetch('/api/auth/reset-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (response.ok) {
                document.getElementById('successModal').classList.add('show');
            } else {
                Utils.showNotification(result.message || 'Password reset failed', 'error');
            }
        } catch (error) {
            console.error('Reset password error:', error);
            Utils.showNotification('Network error. Please try again.', 'error');
        } finally {
            resetBtn.disabled = false;
            resetBtn.textContent = 'Reset Password';
        }
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
                resendBtn.innerHTML = 'Resend Code';
            }
        }, 1000);
    }

    async resendOTP() {
        const email = document.getElementById('resetEmail').value;

        if (!email) {
            Utils.showNotification('Please enter your email address first', 'error');
            return;
        }

        try {
            const response = await fetch('/api/auth/forgot-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email: email })
            });

            if (response.ok) {
                Utils.showNotification('Reset code sent successfully', 'success');
                this.startResendTimer();
            } else {
                Utils.showNotification('Failed to resend code', 'error');
            }
        } catch (error) {
            console.error('Resend OTP error:', error);
            Utils.showNotification('Network error. Please try again.', 'error');
        }
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.resetPasswordManager = new ResetPasswordManager();
});