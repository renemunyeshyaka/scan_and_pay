// Main Application JavaScript
class ScanPayApp {
    constructor() {
        this.currentUser = null;
        this.token = localStorage.getItem('scanpay_token');
        this.init();
    }

    init() {
        this.checkAuthStatus();
        this.setupEventListeners();
        this.setupNavigation();
    }

    checkAuthStatus() {
        if (this.token) {
            this.fetchCurrentUser();
        } else {
            this.showPublicNavigation();
        }
    }

    async fetchCurrentUser() {
        try {
            const response = await fetch('/api/users/me', {
                headers: {
                    'Authorization': `Bearer ${this.token}`
                }
            });

            if (response.ok) {
                this.currentUser = await response.json();
                this.showUserNavigation();
            } else {
                this.handleAuthError();
            }
        } catch (error) {
            console.error('Error fetching user:', error);
            this.handleAuthError();
        }
    }

    showPublicNavigation() {
        document.getElementById('navAuth').style.display = 'flex';
        document.getElementById('navUser').style.display = 'none';
    }

    showUserNavigation() {
        document.getElementById('navAuth').style.display = 'none';
        document.getElementById('navUser').style.display = 'flex';
        document.getElementById('userName').textContent = this.currentUser.name;
    }

    setupEventListeners() {
        // Hamburger menu
        const hamburger = document.getElementById('hamburger');
        const navMenu = document.getElementById('navMenu');

        if (hamburger) {
            hamburger.addEventListener('click', () => {
                navMenu.classList.toggle('active');
            });
        }

        // Get Started button
        const getStartedBtn = document.getElementById('getStartedBtn');
        if (getStartedBtn) {
            getStartedBtn.addEventListener('click', () => {
                window.location.href = '/register.html';
            });
        }
		
		// Get Started button
		       const learnMoreBtn = document.getElementById('learnMoreBtn');
		       if (learnMoreBtn) {
		           learnMoreBtn.addEventListener('click', () => {
		               window.location.href = '/#about';
		           });
		       }

        // Logout button
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                this.logout();
            });
        }

        // Close mobile menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.nav-menu') && !e.target.closest('.hamburger')) {
                navMenu.classList.remove('active');
            }
        });
    }

    setupNavigation() {
        // Smooth scrolling for anchor links
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        });
    }

    logout() {
        localStorage.removeItem('scanpay_token');
        this.token = null;
        this.currentUser = null;
        this.showPublicNavigation();
        window.location.href = '/';
    }

    handleAuthError() {
        localStorage.removeItem('scanpay_token');
        this.token = null;
        this.currentUser = null;
        this.showPublicNavigation();
    }
}

// Utility functions
class Utils {
    static showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <span class="notification-message">${message}</span>
                <button class="notification-close">&times;</button>
            </div>
        `;

        document.body.appendChild(notification);

        // Add styles if not already added
        if (!document.getElementById('notification-styles')) {
            const styles = document.createElement('style');
            styles.id = 'notification-styles';
            styles.textContent = `
                .notification {
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    z-index: 10000;
                    max-width: 400px;
                    animation: slideIn 0.3s ease;
                }
                .notification-content {
                    background: white;
                    padding: 1rem;
                    border-radius: 8px;
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                }
                .notification-success { border-left: 4px solid #10b981; }
                .notification-error { border-left: 4px solid #ef4444; }
                .notification-info { border-left: 4px solid #3b82f6; }
                .notification-warning { border-left: 4px solid #f59e0b; }
                .notification-close {
                    background: none;
                    border: none;
                    font-size: 1.5rem;
                    cursor: pointer;
                    margin-left: 1rem;
                }
                @keyframes slideIn {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
            `;
            document.head.appendChild(styles);
        }

        // Auto remove after 5 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 5000);

        // Close button
        notification.querySelector('.notification-close').addEventListener('click', () => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        });
    }

    static formatCurrency(amount, currency = 'USD') {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency
        }).format(amount);
    }

    static formatDate(date) {
        return new Date(date).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    static debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
}

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.scanPayApp = new ScanPayApp();
});