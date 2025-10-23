class ForgotPasswordManager {
    constructor() {
        this.setupEventListeners();
    }

    setupEventListeners() {
        const form = document.getElementById('forgotPasswordForm');
        if (form) {
            form.addEventListener('submit', (e) => this.handleForgotPassword(e));
        }
    }

    async handleForgotPassword(e) {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData);
        const submitBtn = document.getElementById('submitBtn');

        try {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Sending...';

            const response = await fetch('/api/auth/forgot-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email: data.email })
            });

            const result = await response.json();

            if (response.ok) {
                this.showSuccessModal(data.email);
            } else {
                Utils.showNotification(result.message || 'Failed to send reset code', 'error');
            }
        } catch (error) {
            console.error('Forgot password error:', error);
            Utils.showNotification('Network error. Please try again.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Send Reset Code';
        }
    }

    showSuccessModal(email) {
        document.getElementById('sentEmail').textContent = email;
        document.getElementById('successModal').classList.add('show');
    }
}

// Modal functions
function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('show');
}

// Close modal when clicking outside
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal')) {
        e.target.classList.remove('show');
    }
});

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.forgotPasswordManager = new ForgotPasswordManager();
});