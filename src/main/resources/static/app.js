// Global Application State
const state = {
    token: localStorage.getItem('token') || '',
    userEmail: localStorage.getItem('userEmail') || '',
    userRole: localStorage.getItem('userRole') || '',
    apiBase: '', // served locally from the same host
};

// --- DOM ELEMENTS & EVENT BINDINGS ---
document.addEventListener('DOMContentLoaded', () => {
    initApp();
    setupNavigation();
    setupAuthEvents();
    setupProductEvents();
    setupOrderEvents();
    setupInventoryEvents();
    setupActuatorEvents();
    
    // Check API availability and initial status
    checkApiStatus();
});

// Initialize State and Headers
function initApp() {
    updateAuthUI();
    
    // Setup clear console
    document.getElementById('clear-console-btn').addEventListener('click', () => {
        const consoleDisplay = document.getElementById('console-logs-display');
        consoleDisplay.innerHTML = `
            <div class="log-placeholder">
                <p>No requests recorded yet.</p>
                <p class="small">Interact with any form on the left to execute API queries and see HTTP payloads here.</p>
            </div>
        `;
    });
}

// --- NAVIGATION & TABS ---
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    const tabPanels = document.querySelectorAll('.tab-panel');

    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const targetTab = item.getAttribute('data-tab');
            
            navItems.forEach(nav => nav.classList.remove('active'));
            tabPanels.forEach(panel => panel.classList.remove('active'));
            
            item.classList.add('active');
            const targetPanel = document.getElementById(targetTab);
            if (targetPanel) {
                targetPanel.classList.add('active');
            }
            
            // Tab specific triggers
            if (targetTab === 'products-tab') {
                loadProductsList();
            } else if (targetTab === 'actuator-tab') {
                loadActuatorData();
            }
        });
    });
}

// --- API FETCH WRAPPER WITH LOGGING & CONSOLE PANEL ---
async function apiCall(endpoint, options = {}) {
    const startTime = performance.now();
    const url = `${state.apiBase}${endpoint}`;
    
    // Setup default headers
    options.headers = options.headers || {};
    if (!options.headers['Content-Type'] && !(options.body instanceof FormData)) {
        options.headers['Content-Type'] = 'application/json';
    }
    if (state.token) {
        options.headers['Authorization'] = `Bearer ${state.token}`;
    }

    let requestBodyText = '';
    if (options.body) {
        requestBodyText = typeof options.body === 'string' ? options.body : JSON.stringify(options.body, null, 2);
    }

    let responseData = null;
    let responseText = '';
    let status = 0;
    let statusText = 'Network Error';
    let errorOccurred = false;

    try {
        const response = await fetch(url, options);
        status = response.status;
        statusText = response.statusText;
        responseText = await response.text();
        
        try {
            responseData = responseText ? JSON.parse(responseText) : {};
        } catch (e) {
            responseData = { message: responseText };
        }
    } catch (err) {
        errorOccurred = true;
        statusText = err.message || 'Fetch Failed';
        responseData = { error: err.message };
    }

    const endTime = performance.now();
    const duration = Math.round(endTime - startTime);

    // Log to Right Console
    logRequestToConsole(options.method || 'GET', url, status, statusText, options.headers, requestBodyText, responseData, duration);

    // Process alerts/warnings on the response
    if (errorOccurred || status >= 400) {
        let errMsg = responseData?.message || responseData?.error || 'An unexpected error occurred.';
        if (responseData?.errors && Array.isArray(responseData.errors)) {
            // Spring validation errors format
            errMsg += ' ' + responseData.errors.join(', ');
        }
        showToast(`API Error (${status || 'Network'})`, errMsg, 'error');
        throw { status, data: responseData };
    }

    return responseData;
}

function logRequestToConsole(method, url, status, statusText, headers, requestBody, responseBody, duration) {
    const consoleLogs = document.getElementById('console-logs-display');
    
    // Remove placeholder
    const placeholder = consoleLogs.querySelector('.log-placeholder');
    if (placeholder) {
        placeholder.remove();
    }

    const isSuccess = status >= 200 && status < 300;
    const statusClass = isSuccess ? 'success-status' : 'error-status';
    const methodClass = method.toLowerCase();
    const logId = `log-${Date.now()}-${Math.floor(Math.random() * 1000)}`;

    const logCard = document.createElement('div');
    logCard.className = 'log-card';
    logCard.innerHTML = `
        <div class="log-summary" id="${logId}-summary">
            <div class="log-left">
                <span class="log-time">${new Date().toLocaleTimeString()}</span>
                <span class="log-method ${methodClass}">${method}</span>
                <span class="log-url" title="${url}">${url.replace(window.location.origin, '')}</span>
            </div>
            <div class="log-right">
                <span class="log-status ${statusClass}">${status} ${statusText}</span>
                <span class="log-time" style="margin-left: 8px;">${duration}ms</span>
            </div>
        </div>
        <div class="log-details" id="${logId}-details">
            <div>
                <div class="log-section-title">Request Headers</div>
                <pre class="log-pre">${JSON.stringify(headers, null, 2)}</pre>
            </div>
            ${requestBody ? `
            <div>
                <div class="log-section-title">Request Body</div>
                <pre class="log-pre">${escapeHtml(requestBody)}</pre>
            </div>` : ''}
            <div>
                <div class="log-section-title">Response Payload</div>
                <pre class="log-pre">${escapeHtml(JSON.stringify(responseBody, null, 2))}</pre>
            </div>
        </div>
    `;

    consoleLogs.insertBefore(logCard, consoleLogs.firstChild);

    // Bind Expand/Collapse Event
    const summary = document.getElementById(`${logId}-summary`);
    const details = document.getElementById(`${logId}-details`);
    summary.addEventListener('click', () => {
        summary.classList.toggle('open');
        details.classList.toggle('open');
    });
}

function escapeHtml(text) {
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// --- TOAST NOTIFICATIONS (Alerts/Warnings/Successes) ---
function showToast(title, message, type = 'info', duration = 6000) {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'warning') icon = '⚠️';
    if (type === 'error') icon = '🚨';

    toast.innerHTML = `
        <span class="toast-icon">${icon}</span>
        <div class="toast-content">
            <div class="toast-title">${title}</div>
            <div class="toast-message">${message}</div>
        </div>
        <button class="toast-close">×</button>
    `;

    container.appendChild(toast);
    
    // Trigger animation frame to start slide-in
    requestAnimationFrame(() => {
        toast.classList.add('show');
    });

    const removeToast = () => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    };

    // Close on click close button
    toast.querySelector('.toast-close').addEventListener('click', removeToast);

    // Auto-remove after duration
    const timeoutId = setTimeout(removeToast, duration);
}

// --- AUTHENTICATION MODULE ---
function setupAuthEvents() {
    // Registration Form
    document.getElementById('register-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const firstName = document.getElementById('reg-firstname').value;
        const lastName = document.getElementById('reg-lastname').value;
        const email = document.getElementById('reg-email').value;
        const password = document.getElementById('reg-password').value;
        const phone = document.getElementById('reg-phone').value;
        const role = document.getElementById('reg-role').value;

        const body = { firstName, lastName, email, password, phone, role };
        
        try {
            const res = await apiCall('/api/v1/auth/register', {
                method: 'POST',
                body: body
            });
            showToast('Success', 'User registered successfully!', 'success');
            
            // Automatically log in
            if (res?.data?.accessToken) {
                saveToken(res.data);
            }
        } catch (err) {
            // Handled by apiCall
        }
    });

    // Login Form
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;

        try {
            const res = await apiCall('/api/v1/auth/login', {
                method: 'POST',
                body: { email, password }
            });
            showToast('Success', 'Login successful!', 'success');
            if (res?.data?.accessToken) {
                saveToken(res.data);
            }
        } catch (err) {
            // Handled by apiCall
        }
    });

    // Logout Button
    document.getElementById('logout-btn').addEventListener('click', () => {
        clearToken();
        showToast('Info', 'Logged out successfully.', 'info');
    });

    // Copy Token Button
    document.getElementById('copy-token-btn').addEventListener('click', () => {
        if (!state.token) {
            showToast('Warning', 'No token to copy!', 'warning');
            return;
        }
        navigator.clipboard.writeText(state.token);
        showToast('Success', 'Bearer token copied to clipboard!', 'success');
    });
}

function saveToken(authData) {
    state.token = authData.accessToken;
    // Decode basic properties from payload (or just use responses)
    state.userEmail = authData.email || 'user';
    state.userRole = authData.role || 'CUSTOMER';
    
    localStorage.setItem('token', state.token);
    localStorage.setItem('userEmail', state.userEmail);
    localStorage.setItem('userRole', state.userRole);
    
    updateAuthUI();
}

function clearToken() {
    state.token = '';
    state.userEmail = '';
    state.userRole = '';
    
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    
    updateAuthUI();
}

function updateAuthUI() {
    const tokenDisplay = document.getElementById('token-display');
    const headerEmail = document.getElementById('header-user-email');
    const roleBadge = document.getElementById('role-badge');
    const logoutBtn = document.getElementById('logout-btn');

    if (state.token) {
        tokenDisplay.textContent = `Bearer ${state.token}`;
        headerEmail.textContent = state.userEmail;
        roleBadge.textContent = state.userRole;
        roleBadge.className = `badge ${state.userRole.toLowerCase()}`;
        logoutBtn.classList.remove('hidden');
    } else {
        tokenDisplay.textContent = 'No token active. You are making requests as Guest (Anonymous).';
        headerEmail.textContent = 'Not Logged In';
        roleBadge.textContent = 'GUEST';
        roleBadge.className = 'badge';
        logoutBtn.classList.add('hidden');
    }
}

function quickFill(email, password) {
    document.getElementById('login-email').value = email;
    document.getElementById('login-password').value = password;
    showToast('Credentials Filled', 'Click the Login button to authenticate.', 'info');
}

// --- PRODUCTS MODULE ---
function setupProductEvents() {
    // Load products list button
    document.getElementById('btn-refresh-products').addEventListener('click', loadProductsList);

    // Product Form submit
    document.getElementById('product-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const prodId = document.getElementById('prod-id').value;
        const name = document.getElementById('prod-name').value;
        const description = document.getElementById('prod-description').value;
        const price = parseFloat(document.getElementById('prod-price').value);
        const category = document.getElementById('prod-category').value;
        const active = document.getElementById('prod-active').checked;

        const body = { name, description, price, category, active };
        const method = prodId ? 'PUT' : 'POST';
        const url = prodId ? `/api/v1/products/${prodId}` : '/api/v1/products';

        try {
            await apiCall(url, {
                method: method,
                body: body
            });
            showToast('Success', prodId ? 'Product updated successfully!' : 'Product created successfully!', 'success');
            resetProductForm();
            loadProductsList();
        } catch (err) {
            // Error handled by wrapper
        }
    });

    // Cancel edit product
    document.getElementById('prod-cancel-btn').addEventListener('click', resetProductForm);
}

async function loadProductsList() {
    const tableBody = document.querySelector('#products-table tbody');
    tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Loading products...</td></tr>';
    
    try {
        const res = await apiCall('/api/v1/products');
        const products = res?.data || [];
        
        if (products.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No products found.</td></tr>';
            return;
        }

        tableBody.innerHTML = '';
        products.forEach(p => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${p.id}</td>
                <td><strong>${escapeHtml(p.name)}</strong></td>
                <td>${escapeHtml(p.category)}</td>
                <td>$${p.price.toFixed(2)}</td>
                <td><span class="badge ${p.active ? 'badge-active' : 'badge-inactive'}">${p.active ? 'Active' : 'Inactive'}</span></td>
                <td>
                    <button class="btn btn-outline btn-xs edit-prod-btn" data-id="${p.id}" data-name="${escapeHtml(p.name)}" data-desc="${escapeHtml(p.description || '')}" data-price="${p.price}" data-cat="${escapeHtml(p.category)}" data-active="${p.active}">Edit</button>
                    <button class="btn btn-outline btn-xs delete-prod-btn" style="color:var(--danger); border-color:var(--danger-border);" data-id="${p.id}">Deactivate</button>
                </td>
            `;
            tableBody.appendChild(tr);
        });

        // Bind Edit buttons
        document.querySelectorAll('.edit-prod-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.getElementById('prod-id').value = btn.getAttribute('data-id');
                document.getElementById('prod-name').value = btn.getAttribute('data-name');
                document.getElementById('prod-description').value = btn.getAttribute('data-desc');
                document.getElementById('prod-price').value = btn.getAttribute('data-price');
                document.getElementById('prod-category').value = btn.getAttribute('data-cat');
                document.getElementById('prod-active').checked = btn.getAttribute('data-active') === 'true';
                
                document.getElementById('product-form-title').textContent = 'Edit Product #' + btn.getAttribute('data-id');
                document.getElementById('prod-submit-btn').textContent = 'Update Product';
                document.getElementById('prod-cancel-btn').classList.remove('hidden');
                
                showToast('Edit Mode', `Loaded product ID ${btn.getAttribute('data-id')} into the form.`, 'info');
            });
        });

        // Bind Delete buttons
        document.querySelectorAll('.delete-prod-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                if (confirm(`Are you sure you want to deactivate product ID ${id}?`)) {
                    try {
                        await apiCall(`/api/v1/products/${id}`, { method: 'DELETE' });
                        showToast('Success', `Product ID ${id} deactivated.`, 'success');
                        loadProductsList();
                    } catch (err) {
                        // handled
                    }
                }
            });
        });

    } catch (err) {
        tableBody.innerHTML = '<tr><td colspan="6" class="text-center" style="color:var(--danger);">Failed to load products.</td></tr>';
    }
}

function resetProductForm() {
    document.getElementById('prod-id').value = '';
    document.getElementById('product-form').reset();
    document.getElementById('prod-active').checked = true;
    document.getElementById('product-form-title').textContent = 'Create New Product';
    document.getElementById('prod-submit-btn').textContent = 'Save Product';
    document.getElementById('prod-cancel-btn').classList.add('hidden');
}

// --- ORDERS MODULE ---
let orderItemCount = 1;
function setupOrderEvents() {
    // Add Cart Item
    document.getElementById('btn-add-order-item').addEventListener('click', () => {
        const list = document.getElementById('order-items-list');
        const row = document.createElement('div');
        row.className = 'order-item-row';
        row.setAttribute('data-index', orderItemCount);
        row.innerHTML = `
            <input type="number" class="item-prod-id" required placeholder="Prod ID" style="width: 50%;">
            <input type="number" class="item-qty" min="1" required placeholder="Qty" value="1" style="width: 35%;">
            <button type="button" class="btn-remove-item" onclick="removeOrderItem(this)">×</button>
        `;
        list.appendChild(row);
        orderItemCount++;
    });

    // Place Order Form submit
    document.getElementById('order-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const userId = parseInt(document.getElementById('order-userid').value);
        const idempotencyKey = document.getElementById('order-idempotency').value;
        
        // Assemble items
        const itemRows = document.querySelectorAll('.order-item-row');
        const items = [];
        itemRows.forEach(row => {
            const productId = parseInt(row.querySelector('.item-prod-id').value);
            const quantity = parseInt(row.querySelector('.item-qty').value);
            if (productId && quantity) {
                items.push({ productId, quantity });
            }
        });

        if (items.length === 0) {
            showToast('Warning', 'Cart items cannot be empty!', 'warning');
            return;
        }

        const body = { userId, items };
        if (idempotencyKey) {
            body.idempotencyKey = idempotencyKey;
        }

        try {
            const res = await apiCall('/api/v1/orders', {
                method: 'POST',
                body: body
            });
            showToast('Order Placed', `Order ID ${res?.data?.id} placed successfully!`, 'success');
            
            // Render the returned order
            renderOrdersResults([res.data]);
        } catch (err) {
            // Handled
        }
    });

    // Query single Order
    document.getElementById('btn-get-order').addEventListener('click', async () => {
        const id = document.getElementById('search-order-id').value;
        if (!id) {
            showToast('Warning', 'Please enter an Order ID.', 'warning');
            return;
        }

        try {
            const res = await apiCall(`/api/v1/orders/${id}`);
            if (res?.data) {
                renderOrdersResults([res.data]);
                showToast('Success', `Fetched Order ID ${id}`, 'success');
            } else {
                renderOrdersResults([]);
            }
        } catch (err) {
            renderOrdersResults([]);
        }
    });

    // Query user orders history
    document.getElementById('btn-get-user-orders').addEventListener('click', async () => {
        const userId = document.getElementById('search-user-id').value;
        if (!userId) {
            showToast('Warning', 'Please enter a User ID.', 'warning');
            return;
        }

        try {
            const res = await apiCall(`/api/v1/orders/users/${userId}`);
            const orders = res?.data || [];
            renderOrdersResults(orders);
            showToast('Success', `Fetched ${orders.length} orders for User ID ${userId}`, 'success');
        } catch (err) {
            renderOrdersResults([]);
        }
    });
}

function removeOrderItem(btn) {
    const row = btn.closest('.order-item-row');
    const list = document.getElementById('order-items-list');
    
    // Maintain at least one row
    if (list.querySelectorAll('.order-item-row').length > 1) {
        row.remove();
    } else {
        showToast('Warning', 'You must have at least one item in the cart.', 'warning');
    }
}

function renderOrdersResults(orders) {
    const resultsContainer = document.getElementById('orders-list-results');
    
    if (!orders || orders.length === 0) {
        resultsContainer.innerHTML = `
            <div class="blank-state">
                <p>No orders found matching the criteria.</p>
            </div>
        `;
        return;
    }

    resultsContainer.innerHTML = '';
    orders.forEach(order => {
        const itemsList = order.items.map(item => 
            `<li>Product ID ${item.productId}: ${item.quantity} x $${item.price.toFixed(2)}</li>`
        ).join('');

        const orderCard = document.createElement('div');
        orderCard.className = 'order-card';
        orderCard.innerHTML = `
            <div class="order-card-header">
                <span>Order #${order.id}</span>
                <span class="order-status ${order.status.toLowerCase()}">${order.status}</span>
            </div>
            <div style="font-weight: 500;">Total Amount: $${order.totalPrice.toFixed(2)}</div>
            <div>
                <strong>Items:</strong>
                <ul style="padding-left: 16px; margin-top: 4px;">${itemsList}</ul>
            </div>
            <div class="info-row" style="color:var(--text-muted); font-size:11px; margin-top: 4px;">
                <span>User ID: ${order.userId}</span>
                <span>Date: ${new Date(order.createdAt).toLocaleDateString()}</span>
            </div>
            ${order.status !== 'CANCELLED' ? `
            <button class="btn btn-outline btn-xs mt-2 cancel-order-btn" style="color:var(--danger); border-color:var(--danger-border);" data-id="${order.id}">
                Cancel Order
            </button>` : ''}
        `;
        resultsContainer.appendChild(orderCard);
    });

    // Bind cancel buttons
    resultsContainer.querySelectorAll('.cancel-order-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-id');
            try {
                const res = await apiCall(`/api/v1/orders/${id}/cancel`, { method: 'POST' });
                showToast('Cancelled', `Order ID ${id} was cancelled successfully.`, 'warning');
                // Reload list or view
                // For simplicity, we can fetch single order status again or just rebuild this specific card
                if (res?.data) {
                    // Try to re-query the search to refresh UI
                    document.getElementById('btn-get-order').click();
                }
            } catch (err) {
                // error
            }
        });
    });
}

// --- INVENTORY MODULE ---
function setupInventoryEvents() {
    // Fetch Inventory
    document.getElementById('btn-get-inventory').addEventListener('click', async () => {
        const productId = document.getElementById('inv-productId-check').value;
        if (!productId) {
            showToast('Warning', 'Please enter a Product ID.', 'warning');
            return;
        }

        try {
            const res = await apiCall(`/api/v1/inventory/products/${productId}`);
            const inv = res?.data;
            if (inv) {
                document.getElementById('inv-disp-prod-id').textContent = inv.productId;
                
                const availSpan = document.getElementById('inv-disp-avail');
                availSpan.textContent = inv.availableQuantity;
                
                // Show warning indicator if low stock
                // (low stock is configured in app, but let's say less than 10 is warning)
                if (inv.availableQuantity < 10) {
                    availSpan.className = 'badge badge-inactive';
                    showToast('Low Stock Alert', `Product ID ${inv.productId} only has ${inv.availableQuantity} units left!`, 'warning');
                } else {
                    availSpan.className = 'badge badge-active';
                }

                document.getElementById('inv-disp-reserved').textContent = inv.reservedQuantity;
                document.getElementById('inv-disp-reserved').className = inv.reservedQuantity > 0 ? 'badge badge-inactive' : 'badge';
                
                document.getElementById('inventory-info-card').classList.remove('hidden');
                showToast('Success', 'Inventory fetched.', 'success');
            } else {
                document.getElementById('inventory-info-card').classList.add('hidden');
                showToast('Not Found', 'No inventory record for this product.', 'warning');
            }
        } catch (err) {
            document.getElementById('inventory-info-card').classList.add('hidden');
        }
    });

    // Initialize Inventory
    document.getElementById('inventory-init-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const productId = parseInt(document.getElementById('inv-productId-init').value);
        const availableQuantity = parseInt(document.getElementById('inv-avail-init').value);
        const reservedQuantity = parseInt(document.getElementById('inv-res-init').value);

        try {
            await apiCall('/api/v1/inventory', {
                method: 'POST',
                body: { productId, availableQuantity, reservedQuantity }
            });
            showToast('Success', 'Inventory record created successfully.', 'success');
            document.getElementById('inventory-init-form').reset();
        } catch (err) {
            // error
        }
    });

    // Adjust Stock Count
    document.getElementById('inventory-adjust-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const productId = parseInt(document.getElementById('inv-productId-adj').value);
        const availableQuantity = parseInt(document.getElementById('inv-avail-adj').value);

        try {
            await apiCall(`/api/v1/inventory/products/${productId}/stock`, {
                method: 'PUT',
                body: { availableQuantity }
            });
            showToast('Success', 'Stock count updated successfully.', 'success');
            document.getElementById('inventory-adjust-form').reset();
            
            // Refresh info display if it's currently loaded
            if (document.getElementById('inv-disp-prod-id').textContent == productId) {
                document.getElementById('inv-productId-check').value = productId;
                document.getElementById('btn-get-inventory').click();
            }
        } catch (err) {
            // error
        }
    });

    // Reserve Stock Simulator
    document.getElementById('btn-reserve-stock').addEventListener('click', async () => {
        const productId = parseInt(document.getElementById('inv-productId-sim').value);
        const quantity = parseInt(document.getElementById('inv-qty-sim').value);

        if (!productId || !quantity) {
            showToast('Warning', 'Product ID and Quantity are required.', 'warning');
            return;
        }

        try {
            await apiCall('/api/v1/inventory/reserve', {
                method: 'POST',
                body: { productId, quantity }
            });
            showToast('Stock Reserved', `Reserved ${quantity} units for Product ID ${productId}.`, 'success');
        } catch (err) {
            // error
        }
    });

    // Release Stock Simulator
    document.getElementById('btn-release-stock').addEventListener('click', async () => {
        const productId = parseInt(document.getElementById('inv-productId-sim').value);
        const quantity = parseInt(document.getElementById('inv-qty-sim').value);

        if (!productId || !quantity) {
            showToast('Warning', 'Product ID and Quantity are required.', 'warning');
            return;
        }

        try {
            await apiCall('/api/v1/inventory/release', {
                method: 'POST',
                body: { productId, quantity }
            });
            showToast('Stock Released', `Released ${quantity} units for Product ID ${productId}.`, 'info');
        } catch (err) {
            // error
        }
    });
}

// --- ACTUATOR & DIAGNOSTICS MODULE ---
function setupActuatorEvents() {
    document.getElementById('btn-refresh-actuator').addEventListener('click', loadActuatorData);
    
    // Live Beans Search filtering
    document.getElementById('bean-search').addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        const beansList = document.querySelectorAll('#beans-list-display li');
        
        beansList.forEach(li => {
            const text = li.textContent.toLowerCase();
            if (text.includes(query)) {
                li.style.display = 'block';
            } else {
                li.style.display = 'none';
            }
        });
    });
}

async function loadActuatorData() {
    loadActuatorHealth();
    loadActuatorMetrics();
    loadActuatorBeans();
}

async function loadActuatorHealth() {
    const healthStatusEl = document.getElementById('act-health-status');
    const healthRing = document.getElementById('health-ring-color');
    const dbBadge = document.getElementById('act-health-db');
    const diskBadge = document.getElementById('act-health-disk');
    const mailBadge = document.getElementById('act-health-mail');

    try {
        const res = await apiCall('/actuator/health');
        const status = res?.status || 'UNKNOWN';
        
        healthStatusEl.textContent = status;
        if (status === 'UP') {
            healthRing.className = 'health-ring';
        } else {
            healthRing.className = 'health-ring down';
            showToast('Warning', `System Health status is reporting: ${status}`, 'warning');
        }

        // Details components parsing (Spring Boot format varies, checking components)
        const components = res?.components || {};
        
        const dbStatus = components.db?.status || 'UNKNOWN';
        dbBadge.textContent = dbStatus;
        dbBadge.className = `badge ${dbStatus === 'UP' ? 'badge-active' : 'badge-inactive'}`;

        const diskStatus = components.diskSpace?.status || 'UNKNOWN';
        diskBadge.textContent = diskStatus;
        diskBadge.className = `badge ${diskStatus === 'UP' ? 'badge-active' : 'badge-inactive'}`;

        const mailStatus = components.mail?.status || 'UNKNOWN';
        mailBadge.textContent = mailStatus;
        mailBadge.className = `badge ${mailStatus === 'UP' ? 'badge-active' : 'badge-inactive'}`;
        
    } catch (err) {
        healthStatusEl.textContent = 'DOWN';
        healthRing.className = 'health-ring down';
        dbBadge.className = 'badge badge-inactive';
        diskBadge.className = 'badge badge-inactive';
        mailBadge.className = 'badge badge-inactive';
    }
}

async function loadActuatorMetrics() {
    const memoryVal = document.getElementById('metric-memory');
    const memoryBar = document.getElementById('metric-memory-bar');
    const cpuVal = document.getElementById('metric-cpu');
    const cpuBar = document.getElementById('metric-cpu-bar');

    // 1. JVM Memory Used
    try {
        const res = await apiCall('/actuator/metrics/jvm.memory.used');
        const bytes = res?.measurements?.[0]?.value || 0;
        const mb = (bytes / (1024 * 1024)).toFixed(1);
        memoryVal.textContent = `${mb} MB`;
        
        // Let's set a percentage relative to 512MB max for a pretty UI bar
        const pct = Math.min((mb / 512) * 100, 100);
        memoryBar.style.width = `${pct}%`;
    } catch (err) {
        memoryVal.textContent = 'Unavailable';
        memoryBar.style.width = '0%';
    }

    // 2. CPU Usage
    try {
        const res = await apiCall('/actuator/metrics/system.cpu.usage');
        const val = res?.measurements?.[0]?.value || 0;
        const pct = (val * 100).toFixed(1);
        cpuVal.textContent = `${pct}%`;
        cpuBar.style.width = `${pct}%`;
        
        if (pct > 80) {
            showToast('High CPU Warning', `Actuator CPU usage is reporting very high levels: ${pct}%`, 'warning');
        }
    } catch (err) {
        cpuVal.textContent = 'Unavailable';
        cpuBar.style.width = '0%';
    }
}

async function loadActuatorBeans() {
    const display = document.getElementById('beans-list-display');
    display.innerHTML = '<li>Loading spring beans context...</li>';

    try {
        const res = await apiCall('/actuator/beans');
        // Structure: context -> beans -> bean details
        const contexts = res?.contexts || {};
        let allBeans = [];
        
        for (const contextName in contexts) {
            const beans = contexts[contextName]?.beans || {};
            for (const beanName in beans) {
                allBeans.push({
                    name: beanName,
                    type: beans[beanName]?.type || 'Unknown'
                });
            }
        }

        if (allBeans.length === 0) {
            display.innerHTML = '<li>No beans found in the actuator payload.</li>';
            return;
        }

        // Sort beans by name
        allBeans.sort((a, b) => a.name.localeCompare(b.name));

        display.innerHTML = '';
        allBeans.forEach(b => {
            const li = document.createElement('li');
            li.innerHTML = `<strong>${escapeHtml(b.name)}</strong><br/><span style="color:var(--text-muted); font-size:10px;">${escapeHtml(b.type)}</span>`;
            display.appendChild(li);
        });

    } catch (err) {
        display.innerHTML = '<li style="color:var(--danger);">Failed to load spring beans context from Actuator. Make sure you have correct permissions.</li>';
    }
}

// --- API ONLINE STATUS CHECK ---
async function checkApiStatus() {
    const dot = document.getElementById('api-status-dot');
    const text = document.getElementById('api-status-text');

    try {
        // Ping health endpoint
        const response = await fetch('/actuator/health');
        if (response.ok) {
            dot.className = 'dot green';
            text.textContent = 'API Online';
        } else {
            dot.className = 'dot red';
            text.textContent = 'API Error';
        }
    } catch (err) {
        dot.className = 'dot red';
        text.textContent = 'API Offline';
    }
    
    // Poll status every 15 seconds
    setTimeout(checkApiStatus, 15000);
}
