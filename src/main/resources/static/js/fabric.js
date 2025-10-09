// ============================================
// FABRIC MANAGEMENT - COMPLETE UPDATED JAVASCRIPT
// ============================================

let fabricsList = [];
let movementsList = [];
let fabricChart, fabricTypeChart;
let existingFabricIds = [];
let existingTypes = [];
let existingColors = [];
let selectedFabricStock = 0;
let validationTimeout;

// ============================================
// INITIALIZATION
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    initializeFabricManagement();
});

function initializeFabricManagement() {
    setupEventListeners();
    initializeCharts();

    // Load data sequentially to ensure proper initialization
    loadFabrics()
        .then(() => loadMovements())
        .then(() => {
            loadStats();
            updateCharts();
            populateDynamicDropdowns();
            populateFabricFilters();
            populateMovementFilters();
        })
        .catch(err => {
            console.error('Initialization error:', err);
            showNotification('Failed to initialize: ' + err.message, 'error');
        });
}

// ============================================
// EVENT LISTENERS
// ============================================

function setupEventListeners() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');

    menuToggle?.addEventListener('click', () => {
        sidebar.classList.toggle('open');
        overlay.classList.toggle('show');
    });

    overlay?.addEventListener('click', () => {
        sidebar.classList.remove('open');
        overlay.classList.remove('show');
    });

    // Fabric Modal
    const addFabricBtn = document.getElementById('addNewFabricBtn');
    if (addFabricBtn) {
        addFabricBtn.addEventListener('click', () => {
            document.getElementById('addFabricModal').style.display = 'block';
            document.getElementById('submitFabricBtn').disabled = false;
            populateDynamicDropdowns();
        });
    }

    document.getElementById('closeFabricModal')?.addEventListener('click', closeFabricModalFn);
    document.getElementById('cancelFabricBtn')?.addEventListener('click', closeFabricModalFn);
    document.getElementById('addFabricForm')?.addEventListener('submit', handleFabricSubmit);

    // Edit Fabric Modal
    document.getElementById('closeEditFabricModal')?.addEventListener('click', closeEditFabricModalFn);
    document.getElementById('cancelEditFabricBtn')?.addEventListener('click', closeEditFabricModalFn);
    document.getElementById('editFabricForm')?.addEventListener('submit', handleEditFabricSubmit);

    // Movement Modal
    document.getElementById('addMovementBtn')?.addEventListener('click', () => {
        document.getElementById('addMovementModal').style.display = 'block';
        setTodayDate();
        updateFabricDropdown();
    });
    document.getElementById('closeMovementModal')?.addEventListener('click', closeMovementModalFn);
    document.getElementById('cancelMovementBtn')?.addEventListener('click', closeMovementModalFn);
    document.getElementById('addMovementForm')?.addEventListener('submit', handleMovementSubmit);

    // Approval Modal
    document.getElementById('closeApprovalModal')?.addEventListener('click', closeApprovalModalFn);
    document.getElementById('cancelApprovalBtn')?.addEventListener('click', closeApprovalModalFn);
    document.getElementById('movementApprovalForm')?.addEventListener('submit', handleApprovalSubmit);

    // Dynamic Updates
    document.getElementById('fabricSelect')?.addEventListener('change', updateFabricDetails);
    document.getElementById('movementStatus')?.addEventListener('change', validateMovementType);
    document.getElementById('movementQuantity')?.addEventListener('input', validateQuantity);
    document.getElementById('rejectedQuantity')?.addEventListener('input', toggleRejectReason);

    // Validation
    document.getElementById('newFabricId')?.addEventListener('input', validateFabricId);
    document.getElementById('newFabricId')?.addEventListener('blur', validateFabricId);
    document.getElementById('initialStock')?.addEventListener('input', validateInitialStock);
    document.getElementById('initialStock')?.addEventListener('blur', validateInitialStock);

    // Filters and Sorting
    document.getElementById('sortFabricSelect')?.addEventListener('change', applySortAndFilter);
    document.getElementById('filterFabricType')?.addEventListener('change', applySortAndFilter);
    document.getElementById('filterFabricColor')?.addEventListener('change', applySortAndFilter);
    document.getElementById('sortSelect')?.addEventListener('change', applyMovementSortAndFilter);
    document.getElementById('filterType')?.addEventListener('change', applyMovementSortAndFilter);
    document.getElementById('filterStatus')?.addEventListener('change', applyMovementSortAndFilter);

    // Click Outside Modals
    window.addEventListener('click', (e) => {
        if (e.target === document.getElementById('addFabricModal')) closeFabricModalFn();
        if (e.target === document.getElementById('editFabricModal')) closeEditFabricModalFn();
        if (e.target === document.getElementById('addMovementModal')) closeMovementModalFn();
        if (e.target === document.getElementById('movementApprovalModal')) closeApprovalModalFn();
    });

    // Action Buttons
    document.addEventListener('click', handleActions);
}

// ============================================
// CHARTS INITIALIZATION
// ============================================

function initializeCharts() {
    const fabricCtx = document.getElementById('fabricChart')?.getContext('2d');
    if (fabricCtx) {
        fabricChart = new Chart(fabricCtx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Stock Level (meters)',
                    data: [],
                    backgroundColor: 'rgba(75, 0, 130, 0.8)',
                    borderColor: 'rgba(75, 0, 130, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.1)' } },
                    x: { grid: { color: 'rgba(0,0,0,0.1)' } }
                }
            }
        });
    }

    const typeCtx = document.getElementById('fabricTypeChart')?.getContext('2d');
    if (typeCtx) {
        fabricTypeChart = new Chart(typeCtx, {
            type: 'doughnut',
            data: {
                labels: [],
                datasets: [{
                    data: [],
                    backgroundColor: [
                        'rgba(75, 0, 130, 0.8)', 'rgba(52, 152, 219, 0.8)',
                        'rgba(39, 174, 96, 0.8)', 'rgba(243, 156, 18, 0.8)',
                        'rgba(231, 76, 60, 0.8)', 'rgba(155, 89, 182, 0.8)',
                        'rgba(52, 73, 94, 0.8)'
                    ],
                    borderColor: '#fff',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { padding: 15, usePointStyle: true }
                    }
                }
            }
        });
    }
}

// ============================================
// DATA LOADING - UPDATED
// ============================================

function loadStats() {
    // Calculate stats from loaded data
    const totalFabricTypes = new Set(fabricsList.map(f => f.fabricType)).size;
    const totalMeters = fabricsList.reduce((sum, fabric) => sum + (fabric.currentStock || 0), 0);

    const today = new Date().toISOString().split('T')[0];
    const stockInToday = movementsList
        .filter(m => m.movementDate === today && m.status === 'In')
        .reduce((sum, movement) => sum + (movement.quantity || 0), 0);

    const stockOutToday = movementsList
        .filter(m => m.movementDate === today && (m.status === 'Out' || m.status === 'Special Release'))
        .reduce((sum, movement) => sum + (movement.quantity || 0), 0);

    // Update the UI
    document.getElementById('totalFabricTypes').textContent = totalFabricTypes;
    document.getElementById('totalMeters').textContent = totalMeters.toFixed(2);
    document.getElementById('stockInToday').textContent = stockInToday.toFixed(2);
    document.getElementById('stockOutToday').textContent = stockOutToday.toFixed(2);
}

function loadFabrics() {
    showLoadingState('fabricTableBody', 'Loading fabrics...');

    return fetch('/api/fabrics')
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            fabricsList = Array.isArray(data) ? data : [];
            existingFabricIds = fabricsList.map(f => f.fabricId);
            existingTypes = [...new Set(fabricsList.map(f => f.fabricType))].sort();
            existingColors = [...new Set(fabricsList.map(f => f.color))].sort();

            displayFabrics();
            updateFabricDropdown();
            populateDynamicDropdowns();
            populateFabricFilters();

            return fabricsList;
        })
        .catch(err => {
            console.error('Error loading fabrics:', err);
            document.getElementById('fabricTableBody').innerHTML =
                '<tr><td colspan="5" style="text-align:center;color:#e74c3c;padding:30px;"><i class="fas fa-exclamation-triangle"></i><br>Failed to load fabrics: ' + err.message + '</td></tr>';
            fabricsList = [];
            existingFabricIds = [];
            existingTypes = [];
            existingColors = [];
            return [];
        });
}

function loadMovements() {
    showLoadingState('movementTableBody', 'Loading movements...');

    return fetch('/api/fabrics/movements')
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            movementsList = Array.isArray(data) ? data : [];
            displayMovements();
            populateMovementFilters();
            return movementsList;
        })
        .catch(err => {
            console.error('Error loading movements:', err);
            document.getElementById('movementTableBody').innerHTML =
                '<tr><td colspan="8" style="text-align:center;color:#e74c3c;padding:30px;"><i class="fas fa-exclamation-triangle"></i><br>Failed to load movements: ' + err.message + '</td></tr>';
            movementsList = [];
            return [];
        });
}

function showLoadingState(elementId, message = 'Loading...') {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `
            <tr>
                <td colspan="100%" style="text-align:center;padding:30px;color:#666;">
                    <i class="fas fa-spinner fa-spin" style="font-size:24px;margin-bottom:10px;"></i>
                    <br>${message}
                </td>
            </tr>
        `;
    }
}

// ============================================
// MODAL FUNCTIONS
// ============================================

function closeFabricModalFn() {
    document.getElementById('addFabricModal').style.display = 'none';
    document.getElementById('addFabricForm').reset();
    document.getElementById('fabricIdHelp').style.display = 'block';
    document.getElementById('fabricIdError').style.display = 'none';
    document.getElementById('stockHelp').style.display = 'block';
    document.getElementById('stockError').style.display = 'none';
    document.getElementById('duplicateWarning').style.display = 'none';
    document.getElementById('newFabricId').classList.remove('input-error');
    document.getElementById('initialStock').classList.remove('input-error');
    document.getElementById('submitFabricBtn').disabled = false;
}

function closeEditFabricModalFn() {
    document.getElementById('editFabricModal').style.display = 'none';
    document.getElementById('editFabricForm').reset();
}

function closeMovementModalFn() {
    document.getElementById('addMovementModal').style.display = 'none';
    document.getElementById('addMovementForm').reset();
    document.getElementById('fabricDetails').innerHTML = 'Select a fabric to see details';
    document.getElementById('quantityHelp').style.display = 'block';
    document.getElementById('quantityError').style.display = 'none';
    document.getElementById('movementTypeHelp').style.display = 'none';
    document.getElementById('movementQuantity').classList.remove('input-error');
    document.getElementById('submitMovementBtn').disabled = false;
    selectedFabricStock = 0;
    setTodayDate();
}

function closeApprovalModalFn() {
    const modal = document.getElementById('movementApprovalModal');
    if (modal) {
        modal.style.display = 'none';
        document.getElementById('movementApprovalForm')?.reset();
    }
}

function setTodayDate() {
    const today = new Date().toISOString().split('T')[0];
    const movementDate = document.getElementById('movementDate');
    const decisionDate = document.getElementById('decisionDate');

    if (movementDate) {
        movementDate.value = today;
        movementDate.min = today;
        movementDate.max = today;
    }

    if (decisionDate) {
        decisionDate.value = today;
    }
}

function toggleRejectReason() {
    const rejectedQty = document.getElementById('rejectedQuantity');
    const reasonGroup = document.getElementById('rejectReasonGroup');

    if (rejectedQty && reasonGroup) {
        reasonGroup.style.display = parseFloat(rejectedQty.value) > 0 ? 'block' : 'none';
    }
}

// ============================================
// DISPLAY FUNCTIONS
// ============================================

function displayFabrics() {
    applySortAndFilter();
}

function applySortAndFilter() {
    const sortValue = document.getElementById('sortFabricSelect')?.value || '';
    const typeFilter = document.getElementById('filterFabricType')?.value || '';
    const colorFilter = document.getElementById('filterFabricColor')?.value || '';

    let filteredFabrics = [...fabricsList];

    if (typeFilter) {
        filteredFabrics = filteredFabrics.filter(f => f.fabricType === typeFilter);
    }
    if (colorFilter) {
        filteredFabrics = filteredFabrics.filter(f => f.color === colorFilter);
    }

    if (sortValue === 'stock-desc') {
        filteredFabrics.sort((a, b) => (b.currentStock || 0) - (a.currentStock || 0));
    } else if (sortValue === 'stock-asc') {
        filteredFabrics.sort((a, b) => (a.currentStock || 0) - (b.currentStock || 0));
    } else if (sortValue === 'id-asc') {
        filteredFabrics.sort((a, b) => a.fabricId.localeCompare(b.fabricId));
    } else if (sortValue === 'id-desc') {
        filteredFabrics.sort((a, b) => b.fabricId.localeCompare(a.fabricId));
    }

    displayFabricsFiltered(filteredFabrics);
}

function displayFabricsFiltered(fabrics) {
    const tbody = document.getElementById('fabricTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (fabrics.length > 0) {
        fabrics.forEach(f => {
            const stockClass = f.currentStock <= 10 ? 'style="color: #e74c3c; font-weight: bold;"' : '';
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${f.fabricId}</td>
                <td>${f.fabricType}</td>
                <td>${f.color}</td>
                <td ${stockClass}>${(f.currentStock || 0).toFixed(2)}</td>
                <td class="action-buttons-table">
                    <button class="btn edit btn-edit-fabric" data-id="${f.fabricId}" data-type="${f.fabricType}" data-color="${f.color}">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                    <button class="btn delete btn-delete-fabric" data-id="${f.fabricId}">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    } else {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:#666;padding:30px;">No fabrics match the selected filters</td></tr>';
    }
}

function displayMovements() {
    applyMovementSortAndFilter();
}

function applyMovementSortAndFilter() {
    const sortValue = document.getElementById('sortSelect')?.value || '';
    const typeFilter = document.getElementById('filterType')?.value || '';
    const statusFilter = document.getElementById('filterStatus')?.value || '';

    let filteredMovements = [...movementsList];

    if (typeFilter) {
        filteredMovements = filteredMovements.filter(m => {
            const fabric = fabricsList.find(f => f.fabricId === m.fabricId);
            return fabric && fabric.fabricType === typeFilter;
        });
    }
    if (statusFilter) {
        filteredMovements = filteredMovements.filter(m => m.status === statusFilter);
    }

    if (sortValue === 'date-desc') {
        filteredMovements.sort((a, b) => new Date(b.movementDate) - new Date(a.movementDate));
    } else if (sortValue === 'date-asc') {
        filteredMovements.sort((a, b) => new Date(a.movementDate) - new Date(b.movementDate));
    } else if (sortValue === 'quantity-desc') {
        filteredMovements.sort((a, b) => b.quantity - a.quantity);
    } else if (sortValue === 'quantity-asc') {
        filteredMovements.sort((a, b) => a.quantity - b.quantity);
    }

    displayMovementsFiltered(filteredMovements);
}

function displayMovementsFiltered(movements) {
    const tbody = document.getElementById('movementTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (movements.length > 0) {
        movements.forEach(m => {
            const statusClass = m.status === 'In' ? 'status-in' : 'status-out';
            const statusText = m.status === 'In' ? 'Stock In' :
                m.status === 'Special Release' ? 'Special Release' :
                    'Stock Out';

            let approvalInfo = '<span style="color:#999;">Pending</span>';
            if (m.approvedQuantity !== undefined && m.approvedQuantity !== null) {
                approvalInfo = `<strong>Approved:</strong> ${m.approvedQuantity}m`;
                if (m.rejectedQuantity && m.rejectedQuantity > 0) {
                    approvalInfo += `<br><strong>Rejected:</strong> ${m.rejectedQuantity}m`;
                    if (m.rejectionReason) {
                        approvalInfo += `<br><small style="color:#e74c3c;">${m.rejectionReason}</small>`;
                    }
                }
            }

            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${m.movementId}</td>
                <td>${m.fabricId}</td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                <td>${m.movementDate}</td>
                <td>${(m.quantity || 0).toFixed(2)}</td>
                <td>${m.totalQuantity ? m.totalQuantity.toFixed(2) : 'N/A'}</td>
                <td class="approval-details">${approvalInfo}</td>
                <td class="action-buttons-table">
                    <button class="btn delete btn-delete-movement" data-id="${m.movementId}">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    } else {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#666;padding:30px;">No movements match the selected filters</td></tr>';
    }
}

// ============================================
// CHARTS UPDATE
// ============================================

function updateCharts() {
    if (!fabricChart || !fabricTypeChart) return;

    if (fabricsList.length === 0) {
        fabricChart.data.labels = ['No Data'];
        fabricChart.data.datasets[0].data = [0];
        fabricChart.update();

        fabricTypeChart.data.labels = ['No Data'];
        fabricTypeChart.data.datasets[0].data = [0];
        fabricTypeChart.update();
        return;
    }

    const labels = fabricsList.map(f => `${f.fabricType} - ${f.color}`);
    const stocks = fabricsList.map(f => f.currentStock || 0);

    fabricChart.data.labels = labels;
    fabricChart.data.datasets[0].data = stocks;
    fabricChart.update();

    const typeData = calculateTypeDistribution(labels, stocks);
    fabricTypeChart.data.labels = typeData.labels;
    fabricTypeChart.data.datasets[0].data = typeData.values;
    fabricTypeChart.update();
}

function calculateTypeDistribution(labels, quantities) {
    const typeMap = {};
    labels.forEach((label, index) => {
        const type = label.split(' - ')[0];
        if (!typeMap[type]) typeMap[type] = 0;
        typeMap[type] += quantities[index];
    });
    return {
        labels: Object.keys(typeMap),
        values: Object.values(typeMap)
    };
}

// ============================================
// FILTERS & DROPDOWNS
// ============================================

function populateFabricFilters() {
    const typeFilter = document.getElementById('filterFabricType');
    const colorFilter = document.getElementById('filterFabricColor');

    if (typeFilter) {
        typeFilter.innerHTML = '<option value="">All Types</option>';
        existingTypes.forEach(type => {
            const option = document.createElement('option');
            option.value = type;
            option.textContent = type;
            typeFilter.appendChild(option);
        });
    }

    if (colorFilter) {
        colorFilter.innerHTML = '<option value="">All Colors</option>';
        existingColors.forEach(color => {
            const option = document.createElement('option');
            option.value = color;
            option.textContent = color;
            colorFilter.appendChild(option);
        });
    }
}

function populateMovementFilters() {
    const typeFilter = document.getElementById('filterType');
    if (!typeFilter) return;

    const types = [...new Set(fabricsList.map(f => f.fabricType))].sort();

    typeFilter.innerHTML = '<option value="">All Fabric Types</option>';
    types.forEach(type => {
        const option = document.createElement('option');
        option.value = type;
        option.textContent = type;
        typeFilter.appendChild(option);
    });
}

function updateFabricDropdown() {
    const select = document.getElementById('fabricSelect');
    if (!select) return;

    select.innerHTML = '<option value="">Choose Fabric</option>';

    fabricsList.forEach(f => {
        const option = document.createElement('option');
        option.value = f.fabricId;
        option.textContent = `${f.fabricId} - ${f.fabricType} (${f.color}) - Stock: ${(f.currentStock || 0).toFixed(2)}m`;
        option.dataset.type = f.fabricType;
        option.dataset.color = f.color;
        option.dataset.stock = f.currentStock || 0;
        select.appendChild(option);
    });
}

function populateDynamicDropdowns() {
    populateTypeDropdown();
    populateColorDropdown();
}

function populateTypeDropdown() {
    const typeSelect = document.getElementById('newFabricType');
    if (!typeSelect) return;

    const currentValue = typeSelect.value;
    typeSelect.innerHTML = '';

    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = 'Select Fabric Type';
    typeSelect.appendChild(defaultOption);

    existingTypes.forEach(type => {
        const option = document.createElement('option');
        option.value = type;
        option.textContent = type;
        typeSelect.appendChild(option);
    });

    const separator = document.createElement('option');
    separator.disabled = true;
    separator.textContent = '─────────────────';
    typeSelect.appendChild(separator);

    const addNewOption = document.createElement('option');
    addNewOption.value = '_new_';
    addNewOption.textContent = '➕ Add New Type...';
    addNewOption.style.fontWeight = 'bold';
    addNewOption.style.color = '#4b0082';
    addNewOption.style.backgroundColor = '#f0f0f0';
    typeSelect.appendChild(addNewOption);

    if (currentValue && currentValue !== '_new_') {
        typeSelect.value = currentValue;
    }

    typeSelect.addEventListener('change', function() {
        if (this.value === '_new_') {
            setTimeout(() => {
                const newType = prompt('Enter new fabric type:');
                if (newType && newType.trim()) {
                    const trimmedType = newType.trim();

                    this.innerHTML = '';
                    this.appendChild(defaultOption.cloneNode(true));

                    existingTypes.forEach(type => {
                        const option = document.createElement('option');
                        option.value = type;
                        option.textContent = type;
                        this.appendChild(option);
                    });

                    this.appendChild(separator.cloneNode(true));
                    this.appendChild(addNewOption.cloneNode(true));

                    this.value = trimmedType;
                } else {
                    this.value = '';
                }

                checkDuplicateTypeColor();
            }, 100);
        } else {
            checkDuplicateTypeColor();
        }
    });
}

function populateColorDropdown() {
    const colorSelect = document.getElementById('newFabricColor');
    if (!colorSelect) return;

    const currentValue = colorSelect.value;
    colorSelect.innerHTML = '';

    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = 'Select Color';
    colorSelect.appendChild(defaultOption);

    existingColors.forEach(color => {
        const option = document.createElement('option');
        option.value = color;
        option.textContent = color;
        colorSelect.appendChild(option);
    });

    const separator = document.createElement('option');
    separator.disabled = true;
    separator.textContent = '─────────────────';
    colorSelect.appendChild(separator);

    const addNewOption = document.createElement('option');
    addNewOption.value = '_new_';
    addNewOption.textContent = '➕ Add New Color...';
    addNewOption.style.fontWeight = 'bold';
    addNewOption.style.color = '#4b0082';
    addNewOption.style.backgroundColor = '#f0f0f0';
    colorSelect.appendChild(addNewOption);

    if (currentValue && currentValue !== '_new_') {
        colorSelect.value = currentValue;
    }

    colorSelect.addEventListener('change', function() {
        if (this.value === '_new_') {
            setTimeout(() => {
                const newColor = prompt('Enter new color:');
                if (newColor && newColor.trim()) {
                    const trimmedColor = newColor.trim();

                    this.innerHTML = '';
                    this.appendChild(defaultOption.cloneNode(true));

                    existingColors.forEach(color => {
                        const option = document.createElement('option');
                        option.value = color;
                        option.textContent = color;
                        this.appendChild(option);
                    });

                    this.appendChild(separator.cloneNode(true));
                    this.appendChild(addNewOption.cloneNode(true));

                    this.value = trimmedColor;
                } else {
                    this.value = '';
                }

                checkDuplicateTypeColor();
            }, 100);
        } else {
            checkDuplicateTypeColor();
        }
    });
}

// ============================================
// VALIDATION FUNCTIONS
// ============================================

function validateFabricId() {
    clearTimeout(validationTimeout);

    validationTimeout = setTimeout(() => {
        const input = document.getElementById('newFabricId');
        const helpText = document.getElementById('fabricIdHelp');
        const errorText = document.getElementById('fabricIdError');

        if (!input) return false;

        const fabricId = input.value.trim().toUpperCase();
        input.value = fabricId;

        helpText.style.display = 'block';
        errorText.style.display = 'none';
        input.classList.remove('input-error');

        if (fabricId === '') {
            return false;
        }

        const formatRegex = /^FAB\d{3}$/;
        if (!formatRegex.test(fabricId)) {
            helpText.style.display = 'none';
            errorText.innerHTML = '<i class="fas fa-exclamation-circle"></i> Invalid format! Use FAB followed by 3 digits (e.g., FAB001)';
            errorText.style.display = 'block';
            input.classList.add('input-error');
            return false;
        }

        if (existingFabricIds.includes(fabricId)) {
            helpText.style.display = 'none';
            errorText.innerHTML = '<i class="fas fa-exclamation-circle"></i> This Fabric ID already exists!';
            errorText.style.display = 'block';
            input.classList.add('input-error');
            return false;
        }

        input.style.borderColor = '#27ae60';
        return true;
    }, 300);
}

function validateInitialStock() {
    const input = document.getElementById('initialStock');
    const helpText = document.getElementById('stockHelp');
    const errorText = document.getElementById('stockError');

    if (!input) return false;

    const value = parseFloat(input.value);

    helpText.style.display = 'block';
    errorText.style.display = 'none';
    input.classList.remove('input-error');

    if (isNaN(value) || input.value === '') {
        return false;
    }

    if (value < 10) {
        helpText.style.display = 'none';
        errorText.innerHTML = '<i class="fas fa-exclamation-circle"></i> Initial stock must be at least 10 meters!';
        errorText.style.display = 'block';
        input.classList.add('input-error');
        return false;
    }

    input.style.borderColor = '#27ae60';
    return true;
}

function checkDuplicateTypeColor() {
    const typeSelect = document.getElementById('newFabricType');
    const colorSelect = document.getElementById('newFabricColor');
    const warningDiv = document.getElementById('duplicateWarning');

    if (!typeSelect || !colorSelect || !warningDiv) return false;

    if (!typeSelect.value || !colorSelect.value || typeSelect.value === '_new_' || colorSelect.value === '_new_') {
        warningDiv.style.display = 'none';
        return false;
    }

    const isDuplicate = fabricsList.some(f =>
        f.fabricType === typeSelect.value && f.color === colorSelect.value
    );

    if (isDuplicate) {
        warningDiv.style.display = 'block';
        warningDiv.innerHTML = '<i class="fas fa-exclamation-triangle"></i> This type/color combination already exists! Each fabric must have a unique type and color combination.';
        return true;
    } else {
        warningDiv.style.display = 'none';
        return false;
    }
}

function updateFabricDetails() {
    const select = document.getElementById('fabricSelect');
    const selectedOption = select?.options[select.selectedIndex];
    const detailsDiv = document.getElementById('fabricDetails');
    const statusSelect = document.getElementById('movementStatus');

    if (!select || !detailsDiv || !statusSelect) return;

    if (select.value && selectedOption) {
        const stock = parseFloat(selectedOption.dataset.stock) || 0;
        selectedFabricStock = stock;

        let stockWarning = '';
        if (stock < 10) {
            stockWarning = '<br><span style="color:#e74c3c;"><i class="fas fa-exclamation-triangle"></i> <strong>Low Stock Warning:</strong> Below 10m threshold</span>';
        } else if (stock === 10) {
            stockWarning = '<br><span style="color:#f39c12;"><i class="fas fa-exclamation-triangle"></i> <strong>Critical Stock:</strong> At 10m threshold</span>';
        }

        detailsDiv.innerHTML = `
            <strong>Fabric ID:</strong> ${select.value}<br>
            <strong>Type:</strong> ${selectedOption.dataset.type}<br>
            <strong>Color:</strong> ${selectedOption.dataset.color}<br>
            <strong>Current Stock:</strong> ${stock.toFixed(2)} meters${stockWarning}
        `;

        const currentStatus = statusSelect.value;
        statusSelect.innerHTML = '<option value="">Select Type</option><option value="In">Stock In</option>';

        if (stock <= 10) {
            statusSelect.innerHTML += '<option value="Special Release">Special Release</option>';
        } else {
            statusSelect.innerHTML += '<option value="Out">Stock Out</option><option value="Special Release">Special Release</option>';
        }

        statusSelect.value = currentStatus;
        validateMovementType();
    } else {
        detailsDiv.innerHTML = 'Select a fabric to see details';
        selectedFabricStock = 0;
        statusSelect.innerHTML = '<option value="">Select Type</option><option value="In">Stock In</option><option value="Out">Stock Out</option><option value="Special Release">Special Release</option>';
    }
}

function validateMovementType() {
    const statusSelect = document.getElementById('movementStatus');
    const helpText = document.getElementById('movementTypeHelp');

    if (!statusSelect || !helpText) return;

    if (statusSelect.value === 'Out' && selectedFabricStock <= 10) {
        helpText.innerHTML = '<i class="fas fa-exclamation-circle"></i> Cannot stock out - fabric is at or below 10m threshold. Use Special Release instead.';
        helpText.style.color = '#e74c3c';
        helpText.style.display = 'block';
    } else if (statusSelect.value === 'Special Release' && selectedFabricStock <= 10) {
        helpText.innerHTML = '<i class="fas fa-info-circle"></i> Special Release allows stock out even at or below 10m threshold.';
        helpText.style.color = '#f39c12';
        helpText.style.display = 'block';
    } else if (statusSelect.value === 'Special Release' && selectedFabricStock > 10) {
        helpText.innerHTML = '<i class="fas fa-info-circle"></i> Special Release can be used for emergency stock out.';
        helpText.style.color = '#3498db';
        helpText.style.display = 'block';
    } else {
        helpText.style.display = 'none';
    }
}

function validateQuantity() {
    const input = document.getElementById('movementQuantity');
    const statusSelect = document.getElementById('movementStatus');
    const helpText = document.getElementById('quantityHelp');
    const errorText = document.getElementById('quantityError');

    if (!input || !statusSelect || !helpText || !errorText) return true;

    if (!statusSelect.value) return true;

    const quantity = parseFloat(input.value);

    helpText.style.display = 'block';
    errorText.style.display = 'none';
    input.classList.remove('input-error');

    if (isNaN(quantity) || input.value === '') {
        return true;
    }

    if (quantity <= 0) {
        helpText.style.display = 'none';
        errorText.innerHTML = '<i class="fas fa-exclamation-circle"></i> Quantity must be greater than 0';
        errorText.style.display = 'block';
        input.classList.add('input-error');
        return false;
    }

    if ((statusSelect.value === 'Out' || statusSelect.value === 'Special Release') && quantity > selectedFabricStock) {
        helpText.style.display = 'none';
        errorText.innerHTML = '<i class="fas fa-exclamation-circle"></i> Quantity exceeds available stock!';
        errorText.style.display = 'block';
        input.classList.add('input-error');
        return false;
    }

    input.style.borderColor = '#27ae60';
    return true;
}

function validateMovementBeforeSubmit() {
    const fabricSelect = document.getElementById('fabricSelect');
    const statusSelect = document.getElementById('movementStatus');
    const quantityInput = document.getElementById('movementQuantity');

    if (!fabricSelect?.value) {
        showNotification('Please select a fabric', 'error');
        fabricSelect?.focus();
        return false;
    }

    if (!statusSelect?.value) {
        showNotification('Please select movement type', 'error');
        statusSelect?.focus();
        return false;
    }

    const quantity = parseFloat(quantityInput?.value);
    if (isNaN(quantity) || quantity <= 0) {
        showNotification('Please enter a valid quantity', 'error');
        quantityInput?.focus();
        return false;
    }

    if (statusSelect.value === 'Out' && selectedFabricStock <= 10) {
        showNotification('Cannot stock out - fabric is at or below 10m threshold. Use Special Release instead.', 'error');
        return false;
    }

    if ((statusSelect.value === 'Out' || statusSelect.value === 'Special Release') && quantity > selectedFabricStock) {
        showNotification(`Quantity (${quantity}m) exceeds available stock (${selectedFabricStock}m)!`, 'error');
        quantityInput?.focus();
        return false;
    }

    return true;
}

function validateFabricForm() {
    const fabricIdValid = validateFabricId();
    const stockValid = validateInitialStock();
    const duplicateCheck = !checkDuplicateTypeColor();

    const typeSelect = document.getElementById('newFabricType');
    const colorSelect = document.getElementById('newFabricColor');

    if (!typeSelect.value || typeSelect.value === '_new_') {
        showNotification('Please select a valid fabric type', 'error');
        typeSelect.focus();
        return false;
    }

    if (!colorSelect.value || colorSelect.value === '_new_') {
        showNotification('Please select a valid color', 'error');
        colorSelect.focus();
        return false;
    }

    return fabricIdValid && stockValid && duplicateCheck;
}

// ============================================
// FORM SUBMISSION HANDLERS - UPDATED
// ============================================

function handleFabricSubmit(e) {
    e.preventDefault();

    if (!validateFabricForm()) {
        return;
    }

    const form = e.target;
    const fabricId = form.fabricId.value.trim().toUpperCase();
    const fabricType = form.fabricType.value.trim();
    const color = form.color.value.trim();
    const initialStock = parseFloat(form.initialStock.value);

    if (!fabricId || !fabricType || !color || isNaN(initialStock)) {
        showNotification('Please fill in all required fields', 'error');
        return;
    }

    const data = {
        fabricId: fabricId,
        fabricType: fabricType,
        color: color,
        currentStock: initialStock
    };

    const submitBtn = document.getElementById('submitFabricBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Adding...';

    // Create fabric with initial stock
    fetch('/api/fabrics/new?initialStock=' + initialStock, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(resp => {
            if (resp) {
                showNotification(`Fabric ${fabricId} added successfully with initial stock!`, 'success');
                closeFabricModalFn();
                refreshAllData();
            } else {
                throw new Error('Failed to create fabric');
            }
        })
        .catch(err => {
            console.error(err);
            showNotification('Error: ' + err.message, 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-plus-circle"></i> Add Fabric';
        });
}

function handleEditFabricSubmit(e) {
    e.preventDefault();
    const form = e.target;
    const fabricId = form.fabricId.value;

    const data = {
        fabricId: fabricId,
        fabricType: form.fabricType.value.trim(),
        color: form.color.value.trim(),
        currentStock: 0 // Stock is managed through movements
    };

    const submitBtn = e.submitter || form.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Updating...';
    }

    fetch('/api/fabrics/update', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(resp => {
            if (resp) {
                showNotification('Fabric updated successfully!', 'success');
                closeEditFabricModalFn();
                refreshAllData();
            } else {
                showNotification('Error updating fabric', 'error');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fas fa-edit"></i> Update Fabric';
                }
            }
        })
        .catch(err => {
            console.error(err);
            showNotification('Server error: ' + err.message, 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-edit"></i> Update Fabric';
            }
        });
}

function generateMovementId() {
    if (movementsList.length === 0) return 'FMI001';
    const lastId = movementsList.map(m => m.movementId).sort().pop();
    const num = parseInt(lastId.substring(3)) + 1;
    return 'FMI' + String(num).padStart(3, '0');
}

function handleMovementSubmit(e) {
    e.preventDefault();

    if (!validateMovementBeforeSubmit()) {
        return;
    }

    const form = e.target;
    const fabricId = form.fabricId.value;
    const status = form.status.value;
    const quantity = parseFloat(form.quantity.value);

    const movementId = generateMovementId();

    const data = {
        movementId: movementId,
        fabricId: fabricId,
        status: status,
        movementDate: form.movementDate.value,
        quantity: quantity
    };

    const submitBtn = document.getElementById('submitMovementBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Recording...';
    }

    fetch('/api/fabrics/movements/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(resp => {
            if (resp) {
                showNotification(`Movement ${movementId} recorded successfully!`, 'success');
                closeMovementModalFn();
                refreshAllData();
            } else {
                showNotification('Error recording movement', 'error');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fas fa-exchange-alt"></i> Record Movement';
                }
            }
        })
        .catch(err => {
            console.error(err);
            showNotification('Server error: ' + err.message, 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-exchange-alt"></i> Record Movement';
            }
        });
}

function handleApprovalSubmit(e) {
    e.preventDefault();

    const form = e.target;
    const movementId = form.movementId.value;
    const approvedQuantity = parseFloat(form.approvedQuantity.value);
    const rejectedQuantity = parseFloat(form.rejectedQuantity.value) || 0;
    const rejectReason = form.rejectReason.value.trim();

    if (isNaN(approvedQuantity) || approvedQuantity < 0) {
        showNotification('Please enter a valid approved quantity', 'error');
        return;
    }

    const data = {
        movementId: movementId,
        fabricId: document.getElementById('approvalFabricId').value,
        approvedQuantity: approvedQuantity,
        rejectedQuantity: rejectedQuantity,
        approvalStatus: 'APPROVED',
        rejectionReason: rejectReason
    };

    const submitBtn = document.getElementById('submitApprovalBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Submitting...';
    }

    fetch('/api/fabrics/movements/approve', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(resp => {
            if (resp) {
                showNotification('Movement approval recorded successfully!', 'success');
                closeApprovalModalFn();
                refreshAllData();
            } else {
                showNotification('Error recording approval', 'error');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fas fa-check-circle"></i> Submit Decision';
                }
            }
        })
        .catch(err => {
            console.error(err);
            showNotification('Server error: ' + err.message, 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-check-circle"></i> Submit Decision';
            }
        });
}

// ============================================
// ACTION HANDLERS
// ============================================

function handleActions(e) {
    const editFabricBtn = e.target.closest('.btn-edit-fabric');
    const deleteFabricBtn = e.target.closest('.btn-delete-fabric');
    const deleteMovementBtn = e.target.closest('.btn-delete-movement');
    const approveBtn = e.target.closest('.approve-btn');

    if (editFabricBtn) {
        const id = editFabricBtn.dataset.id;
        const type = editFabricBtn.dataset.type;
        const color = editFabricBtn.dataset.color;

        document.getElementById('editFabricId').value = id;
        document.getElementById('editFabricType').value = type;
        document.getElementById('editFabricColor').value = color;
        document.getElementById('editFabricModal').style.display = 'block';
    }

    if (deleteFabricBtn) {
        const id = deleteFabricBtn.dataset.id;
        showConfirmDialog(
            `Are you sure you want to delete fabric <strong>${id}</strong>?<br><br>This will also delete all associated movements and cannot be undone.`,
            () => {
                deleteFabricBtn.disabled = true;
                deleteFabricBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';

                fetch(`/api/fabrics/delete/${id}`, { method: 'DELETE' })
                    .then(res => {
                        if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
                        return res.json();
                    })
                    .then(resp => {
                        if (resp) {
                            showNotification('Fabric deleted successfully', 'success');
                            refreshAllData();
                        } else {
                            showNotification('Error deleting fabric', 'error');
                            deleteFabricBtn.disabled = false;
                            deleteFabricBtn.innerHTML = '<i class="fas fa-trash"></i> Delete';
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        showNotification('Error deleting fabric: ' + err.message, 'error');
                        deleteFabricBtn.disabled = false;
                        deleteFabricBtn.innerHTML = '<i class="fas fa-trash"></i> Delete';
                    });
            }
        );
    }

    if (deleteMovementBtn) {
        const id = deleteMovementBtn.dataset.id;
        showConfirmDialog(
            `Are you sure you want to delete movement <strong>${id}</strong>?<br><br>This will affect stock calculations and cannot be undone.`,
            () => {
                deleteMovementBtn.disabled = true;
                deleteMovementBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';

                fetch(`/api/fabrics/movements/delete/${id}`, { method: 'DELETE' })
                    .then(res => {
                        if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
                        return res.json();
                    })
                    .then(resp => {
                        if (resp) {
                            showNotification('Movement deleted successfully', 'success');
                            refreshAllData();
                        } else {
                            showNotification('Error deleting movement', 'error');
                            deleteMovementBtn.disabled = false;
                            deleteMovementBtn.innerHTML = '<i class="fas fa-trash"></i> Delete';
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        showNotification('Error deleting movement: ' + err.message, 'error');
                        deleteMovementBtn.disabled = false;
                        deleteMovementBtn.innerHTML = '<i class="fas fa-trash"></i> Delete';
                    });
            }
        );
    }

    if (approveBtn) {
        const movementId = approveBtn.dataset.movementId;
        const fabricId = approveBtn.dataset.fabricId;
        const quantity = approveBtn.dataset.quantity;

        document.getElementById('approvalMovementId').value = movementId;
        document.getElementById('approvalFabricId').value = fabricId;
        document.getElementById('approvalTotalQuantity').value = quantity;
        document.getElementById('movementApprovalModal').style.display = 'block';
    }
}

// ============================================
// UTILITY FUNCTIONS - UPDATED
// ============================================

function refreshAllData() {
    // Reload in sequence to ensure stats calculation works
    loadFabrics()
        .then(() => loadMovements())
        .then(() => {
            loadStats();
            updateCharts();
            populateDynamicDropdowns();
            populateFabricFilters();
            populateMovementFilters();
        })
        .catch(err => {
            console.error('Error refreshing data:', err);
            showNotification('Failed to refresh data: ' + err.message, 'error');
        });
}

function showConfirmDialog(message, onConfirm) {
    const existingDialog = document.getElementById('confirmDialog');
    if (existingDialog) existingDialog.remove();

    const dialog = document.createElement('div');
    dialog.id = 'confirmDialog';
    dialog.className = 'modal';
    dialog.style.display = 'block';
    dialog.innerHTML = `
        <div class="modal-content" style="max-width:450px;margin-top:150px;">
            <div class="modal-header">
                <h3 class="modal-title">
                    <i class="fas fa-exclamation-triangle"></i> Confirm Action
                </h3>
            </div>
            <div style="padding:25px;">
                <p style="margin-bottom:20px;color:#555;line-height:1.6;">${message}</p>
                <div class="form-buttons">
                    <button type="button" class="btn btn-cancel" id="confirmCancel">
                        <i class="fas fa-times"></i> Cancel
                    </button>
                    <button type="button" class="btn delete" id="confirmOk">
                        <i class="fas fa-check"></i> Confirm
                    </button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(dialog);

    document.getElementById('confirmCancel').onclick = () => dialog.remove();
    document.getElementById('confirmOk').onclick = () => {
        dialog.remove();
        onConfirm();
    };
    dialog.onclick = (e) => {
        if (e.target === dialog) dialog.remove();
    };
}

function showNotification(msg, type = 'info') {
    document.querySelector('.notification')?.remove();

    const iconMap = {
        success: 'check-circle',
        error: 'exclamation-triangle',
        warning: 'exclamation-circle',
        info: 'info-circle'
    };

    const n = document.createElement('div');
    n.className = `notification notification-${type}`;
    n.innerHTML = `
        <div class="notification-content">
            <i class="fas fa-${iconMap[type] || 'info-circle'}"></i>
            <span>${msg}</span>
        </div>
    `;

    document.body.appendChild(n);

    setTimeout(() => n.classList.add('show'), 100);
    setTimeout(() => {
        n.classList.remove('show');
        setTimeout(() => n.remove(), 300);
    }, 4000);
}

// ============================================
// KEYBOARD SHORTCUTS
// ============================================

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal').forEach(m => m.style.display = 'none');
        document.getElementById('confirmDialog')?.remove();
    }

    if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
        e.preventDefault();
        document.getElementById('addNewFabricBtn')?.click();
    }

    if ((e.ctrlKey || e.metaKey) && e.key === 'm') {
        e.preventDefault();
        document.getElementById('addMovementBtn')?.click();
    }
});