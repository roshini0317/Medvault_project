document.addEventListener('DOMContentLoaded', function() {
    initTabs();
    initModals();
    initFileUpload();
    initAlerts();
    initFormValidation();
});

function initTabs() {
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabGroup = this.closest('.tabs');
            const contentContainer = tabGroup.nextElementSibling;
            
            tabGroup.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            
            const targetId = this.getAttribute('data-tab');
            if (contentContainer && contentContainer.classList.contains('tab-contents')) {
                contentContainer.querySelectorAll('.tab-content').forEach(content => {
                    content.classList.remove('active');
                });
                const targetContent = document.getElementById(targetId);
                if (targetContent) {
                    targetContent.classList.add('active');
                }
            }
        });
    });
}

function initModals() {
    const modalTriggers = document.querySelectorAll('[data-modal]');
    modalTriggers.forEach(trigger => {
        trigger.addEventListener('click', function(e) {
            e.preventDefault();
            const modalId = this.getAttribute('data-modal');
            const modal = document.getElementById(modalId);
            if (modal) {
                modal.classList.add('show');
            }
        });
    });

    const modalCloses = document.querySelectorAll('.modal-close, .modal-backdrop');
    modalCloses.forEach(close => {
        close.addEventListener('click', function() {
            const modal = this.closest('.modal');
            if (modal) {
                modal.classList.remove('show');
            }
        });
    });

    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                this.classList.remove('show');
            }
        });
    });
}

function initFileUpload() {
    const fileUploads = document.querySelectorAll('.file-upload');
    fileUploads.forEach(upload => {
        const input = upload.querySelector('input[type="file"]');
        const label = upload.querySelector('.file-label');
        
        upload.addEventListener('click', function() {
            if (input) input.click();
        });
        
        if (input) {
            input.addEventListener('change', function() {
                if (this.files.length > 0) {
                    if (label) {
                        label.textContent = this.files[0].name;
                    }
                    upload.classList.add('has-file');
                }
            });
        }
        
        upload.addEventListener('dragover', function(e) {
            e.preventDefault();
            this.classList.add('dragover');
        });
        
        upload.addEventListener('dragleave', function() {
            this.classList.remove('dragover');
        });
        
        upload.addEventListener('drop', function(e) {
            e.preventDefault();
            this.classList.remove('dragover');
            if (input && e.dataTransfer.files.length > 0) {
                input.files = e.dataTransfer.files;
                if (label) {
                    label.textContent = e.dataTransfer.files[0].name;
                }
                upload.classList.add('has-file');
            }
        });
    });
}

function initAlerts() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity 0.5s ease';
            setTimeout(() => {
                alert.remove();
            }, 500);
        }, 5000);
    });
}

function initFormValidation() {
    const forms = document.querySelectorAll('form[data-validate]');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            let isValid = true;
            const requiredFields = form.querySelectorAll('[required]');
            
            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    isValid = false;
                    field.classList.add('error');
                    showFieldError(field, 'This field is required');
                } else {
                    field.classList.remove('error');
                    removeFieldError(field);
                }
            });
            
            const emailFields = form.querySelectorAll('input[type="email"]');
            emailFields.forEach(field => {
                if (field.value && !isValidEmail(field.value)) {
                    isValid = false;
                    field.classList.add('error');
                    showFieldError(field, 'Please enter a valid email');
                }
            });
            
            if (!isValid) {
                e.preventDefault();
            }
        });
    });
}

function showFieldError(field, message) {
    let errorEl = field.parentElement.querySelector('.field-error');
    if (!errorEl) {
        errorEl = document.createElement('span');
        errorEl.className = 'field-error';
        errorEl.style.color = '#ef4444';
        errorEl.style.fontSize = '12px';
        errorEl.style.marginTop = '4px';
        errorEl.style.display = 'block';
        field.parentElement.appendChild(errorEl);
    }
    errorEl.textContent = message;
}

function removeFieldError(field) {
    const errorEl = field.parentElement.querySelector('.field-error');
    if (errorEl) {
        errorEl.remove();
    }
}

function isValidEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function confirmAction(message) {
    return confirm(message);
}

function formatDate(dateString) {
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(dateString).toLocaleDateString(undefined, options);
}

function formatTime(timeString) {
    const options = { hour: '2-digit', minute: '2-digit' };
    return new Date('2000-01-01T' + timeString).toLocaleTimeString(undefined, options);
}

function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type}`;
    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.zIndex = '9999';
    notification.style.maxWidth = '400px';
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transition = 'opacity 0.5s ease';
        setTimeout(() => {
            notification.remove();
        }, 500);
    }, 4000);
}

function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    if (sidebar) {
        sidebar.classList.toggle('show');
    }
}

function filterTable(inputId, tableId) {
    const input = document.getElementById(inputId);
    const table = document.getElementById(tableId);
    
    if (!input || !table) return;
    
    const filter = input.value.toLowerCase();
    const rows = table.querySelectorAll('tbody tr');
    
    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(filter) ? '' : 'none';
    });
}

function printElement(elementId) {
    const element = document.getElementById(elementId);
    if (!element) return;
    
    const printWindow = window.open('', '_blank');
    printWindow.document.write('<html><head><title>Print</title>');
    printWindow.document.write('<link rel="stylesheet" href="/css/style.css">');
    printWindow.document.write('</head><body>');
    printWindow.document.write(element.innerHTML);
    printWindow.document.write('</body></html>');
    printWindow.document.close();
    printWindow.print();
}
