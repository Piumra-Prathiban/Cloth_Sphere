// ============================================
// FABRIC MANAGEMENT - FIXED JAVASCRIPT
// ============================================

let fabricsList = [];
let movementsList = [];
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

    // Load initial data and ensure stats are calculated
    Promise.all([
        loadFabricsAsync(),
        loadMovementsAsync()
    ]).then(() => {
        console.log('Both fabrics and movements loaded, calculating stats...');
        calculateAndDisplayStats();
    }).catch(err => {
        console.error('Error loading initial data:', err);
        // Still try to calculate stats with whatever data we have
        calculateAndDisplayStats();
    });
}

// Helper functions for Promise-based loading
function loadFabricsAsync() {
    return new Promise((resolve, reject) => {
        fetch('/api/fabrics')
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
                resolve();
            })
            .catch(err => {
                console.error('Error loading fabrics:', err);
                fabricsList = [];
                reject(err);
            });
    });
}

function loadMovementsAsync() {
    return new Promise((resolve, reject) => {
        fetch('/api/fabrics/movements')
            .then(res => {
                if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
                return res.json();
            })
            .then(data => {
                movementsList = Array.isArray(data) ? data : [];
                displayMovements();
                populateMovementFilters();
                resolve();
            })
            .catch(err => {
                console.error('Error loading movements:', err);
                movementsList = [];
                reject(err);
            });
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
            generateFabricId(); // Auto-generate Fabric ID
        });
    }

    document.getElementById('closeFabricModal')?.addEventListener('click', closeFabricModalFn);
    document.getElementById('cancelFabricBtn')?.addEventListener('click', closeFabricModalFn);

    const addFabricForm = document.getElementById('addFabricForm');
    if (addFabricForm) {
        addFabricForm.addEventListener('submit', handleFabricSubmit);
    }

    // Movement Modal
    document.getElementById('addMovementBtn')?.addEventListener('click', () => {
        document.getElementById('addMovementModal').style.display = 'block';
        setTodayDate();
        updateFabricDropdown();
    });
    document.getElementById('closeMovementModal')?.addEventListener('click', closeMovementModalFn);
    document.getElementById('cancelMovementBtn')?.addEventListener('click', closeMovementModalFn);

    const addMovementForm = document.getElementById('addMovementForm');
    if (addMovementForm) {
        addMovementForm.addEventListener('submit', handleMovementSubmit);
    }

    // Approval Modal
    document.getElementById('closeApprovalModal')?.addEventListener('click', closeApprovalModalFn);
    document.getElementById('cancelApprovalBtn')?.addEventListener('click', closeApprovalModalFn);

    const movementApprovalForm = document.getElementById('movementApprovalForm');
    if (movementApprovalForm) {
        movementApprovalForm.addEventListener('submit', handleApprovalSubmit);
    }

    // Dynamic Updates
    document.getElementById('fabricSelect')?.addEventListener('change', updateFabricDetails);
    document.getElementById('movementStatus')?.addEventListener('change', validateMovementType);
    document.getElementById('movementQuantity')?.addEventListener('input', validateQuantity);
    document.getElementById('rejectedQuantity')?.addEventListener('input', toggleRejectReason);
    document.getElementById('approvedQuantity')?.addEventListener('input', updateRejectedQuantity);

    // Validation
    document.getElementById('newFabricType')?.addEventListener('change', checkDuplicateTypeColor);
    document.getElementById('newFabricColor')?.addEventListener('change', checkDuplicateTypeColor);

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
        if (e.target === document.getElementById('addMovementModal')) closeMovementModalFn();
        if (e.target === document.getElementById('movementApprovalModal')) closeApprovalModalFn();
    });

    // Action Buttons
    document.addEventListener('click', handleActions);
}

// ============================================
// AUTO-GENERATE FABRIC ID
// ============================================

function generateFabricId() {
    const fabricIdInput = document.getElementById('newFabricId');
    if (!fabricIdInput) return;

    // Find the highest existing number
    let maxNum = 0;
    existingFabricIds.forEach(id => {
        const match = id.match(/FAB(\d{3})/);
        if (match) {
            const num = parseInt(match[1]);
            if (num > maxNum) maxNum = num;
        }
    });

    // Generate next ID
    const nextNum = maxNum + 1;
    const nextId = `FAB${String(nextNum).padStart(3, '0')}`;
    fabricIdInput.value = nextId;
    fabricIdInput.readOnly = true; // Make it read-only
}

// ============================================
// DATA LOADING
// ============================================

function loadStats() {
    document.getElementById('totalFabricTypes').textContent = '0';
    document.getElementById('totalMeters').textContent = '0.00';
    document.getElementById('stockInToday').textContent = '0.00';
    document.getElementById('stockOutToday').textContent = '0.00';

    fetch('/api/fabrics/stats')
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            document.getElementById('totalFabricTypes').textContent = data.totalFabricTypes || 0;
            document.getElementById('totalMeters').textContent = (data.totalMeters || 0.0).toFixed(2);
            document.getElementById('stockInToday').textContent = (data.stockInToday || 0.0).toFixed(2);
            document.getElementById('stockOutToday').textContent = (data.stockOutToday || 0.0).toFixed(2);
        })
        .catch(err => {
            console.error('Error loading stats:', err);
            if (fabricsList.length > 0 || movementsList.length > 0) {
                calculateAndDisplayStats();
            }
        });
}

function loadFabrics() {
    showLoadingState('fabricTableBody', 'Loading fabrics...');
    console.log('Loading fabrics from API...');

    fetch('/api/fabrics')
        .then(res => {
            console.log('Fabrics API response status:', res.status);
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            console.log('Loaded fabrics:', data);
            fabricsList = Array.isArray(data) ? data : [];
            existingFabricIds = fabricsList.map(f => f.fabricId);
            existingTypes = [...new Set(fabricsList.map(f => f.fabricType))].sort();
            existingColors = [...new Set(fabricsList.map(f => f.color))].sort();

            displayFabrics();
            updateFabricDropdown();
            populateDynamicDropdowns();
            populateFabricFilters();

            // After fabrics load, check if movements are also loaded and then calculate stats
            if (movementsList.length > 0) {
                calculateAndDisplayStats();
            }
        })
        .catch(err => {
            console.error('Error loading fabrics:', err);
            document.getElementById('fabricTableBody').innerHTML =
                '<tr><td colspan="5" style="text-align:center;color:#e74c3c;padding:30px;">Failed to load fabrics</td></tr>';
            fabricsList = [];
            // Even if fabrics fail, try to calculate with empty data
            calculateAndDisplayStats();
        });
}

function loadMovements() {
    showLoadingState('movementTableBody', 'Loading movements...');
    console.log('Loading movements from API...');

    fetch('/api/fabrics/movements')
        .then(res => {
            console.log('Movements API response status:', res.status);
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            console.log('Loaded movements:', data);
            movementsList = Array.isArray(data) ? data : [];
            displayMovements();
            populateMovementFilters();

            // After movements load, check if fabrics are also loaded and then calculate stats
            if (fabricsList.length > 0) {
                calculateAndDisplayStats();
            }
        })
        .catch(err => {
            console.error('Error loading movements:', err);
            document.getElementById('movementTableBody').innerHTML =
                '<tr><td colspan="10" style="text-align:center;color:#e74c3c;padding:30px;">Failed to load movements</td></tr>';
            movementsList = [];
            // Even if movements fail, try to calculate with available data
            calculateAndDisplayStats();
        });
}

function showLoadingState(elementId, message = 'Loading...') {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `
            <tr>
                <td colspan="100%" style="text-align:center;padding:30px;color:#666;">
                    <i class="fas fa-spinner fa-spin" style="font-size:24px;"></i><br>${message}
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
    document.getElementById('duplicateWarning').style.display = 'none';
    document.getElementById('submitFabricBtn').disabled = false;
    document.getElementById('submitFabricBtn').textContent = 'Add Fabric';
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
    document.getElementById('submitMovementBtn').textContent = 'Record Movement';
    selectedFabricStock = 0;
}

function closeApprovalModalFn() {
    const modal = document.getElementById('movementApprovalModal');
    if (modal) {
        modal.style.display = 'none';
        document.getElementById('movementApprovalForm')?.reset();
        document.getElementById('rejectReasonGroup').style.display = 'none';
        document.getElementById('submitApprovalBtn').disabled = false;
        document.getElementById('submitApprovalBtn').textContent = 'Submit Decision';
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

function updateRejectedQuantity() {
    const approvedQty = parseFloat(document.getElementById('approvedQuantity').value) || 0;
    const totalQty = parseFloat(document.getElementById('approvalTotalQuantity').value) || 0;
    const rejectedQty = document.getElementById('rejectedQuantity');

    if (rejectedQty && approvedQty <= totalQty) {
        const calculatedRejected = totalQty - approvedQty;
        rejectedQty.value = calculatedRejected > 0 ? calculatedRejected.toFixed(2) : 0;
        toggleRejectReason();
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

    if (typeFilter) filteredFabrics = filteredFabrics.filter(f => f.fabricType === typeFilter);
    if (colorFilter) filteredFabrics = filteredFabrics.filter(f => f.color === colorFilter);

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
            // Normalize status for display
            const status = m.status.toUpperCase();
            const statusClass = status === 'IN' ? 'status-in' : 'status-out';
            const statusText = status === 'IN' ? 'Stock In' : 'Stock Out';

            // Approval status display
            let approvalInfo = '<span style="color:#f39c12;">Pending</span>';
            let approvedQty = 'N/A';
            let rejectedQty = 'N/A';
            let rejectReason = 'N/A';

            // Check if movement is approved
            if (m.approvalStatus === 'APPROVED') {
                approvalInfo = `<span style="color:#27ae60;">Approved</span>`;
                approvedQty = m.approvedQuantity ? parseFloat(m.approvedQuantity).toFixed(2) : '0.00';
                rejectedQty = m.rejectedQuantity ? parseFloat(m.rejectedQuantity).toFixed(2) : '0.00';
                rejectReason = m.rejectionReason || 'N/A';
            }

            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${m.movementId}</td>
                <td>${m.fabricId}</td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                <td>${m.movementDate}</td>
                <td>${(parseFloat(m.quantity) || 0).toFixed(2)}</td>
                <td class="approval-details">${approvalInfo}</td>
                <td>${approvedQty}</td>
                <td>${rejectedQty}</td>
                <td>${rejectReason}</td>
                <td class="action-buttons-table">
                    ${m.approvalStatus !== 'APPROVED' ?
                `<button class="btn add approve-btn" data-movement-id="${m.movementId}" data-fabric-id="${m.fabricId}" data-quantity="${m.quantity}" data-status="${m.status}">
                            <i class="fas fa-check"></i> Approve
                        </button>` : ''}
                    <button class="btn delete btn-delete-movement" data-id="${m.movementId}">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    } else {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align:center;color:#666;padding:30px;">No movements match the selected filters</td></tr>';
    }
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
    separator.textContent = '──────────';
    typeSelect.appendChild(separator);

    const addNewOption = document.createElement('option');
    addNewOption.value = '_new_';
    addNewOption.textContent = '+ Add New Type...';
    addNewOption.style.fontWeight = 'bold';
    addNewOption.style.color = '#4b0082';
    typeSelect.appendChild(addNewOption);

    if (currentValue && currentValue !== '_new_') {
        typeSelect.value = currentValue;
    }

    typeSelect.removeEventListener('change', handleTypeChange);
    typeSelect.addEventListener('change', handleTypeChange);
}

function handleTypeChange(e) {
    const typeSelect = e.target;
    if (typeSelect.value === '_new_') {
        setTimeout(() => {
            const newType = prompt('Enter new fabric type:');
            if (newType && newType.trim()) {
                const trimmedType = newType.trim();

                const option = document.createElement('option');
                option.value = trimmedType;
                option.textContent = trimmedType;

                const separator = typeSelect.querySelector('option[disabled]');
                typeSelect.insertBefore(option, separator);
                typeSelect.value = trimmedType;
            } else {
                typeSelect.value = '';
            }
            checkDuplicateTypeColor();
        }, 100);
    } else {
        checkDuplicateTypeColor();
    }
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
    separator.textContent = '──────────';
    colorSelect.appendChild(separator);

    const addNewOption = document.createElement('option');
    addNewOption.value = '_new_';
    addNewOption.textContent = '+ Add New Color...';
    addNewOption.style.fontWeight = 'bold';
    addNewOption.style.color = '#4b0082';
    colorSelect.appendChild(addNewOption);

    if (currentValue && currentValue !== '_new_') {
        colorSelect.value = currentValue;
    }

    colorSelect.removeEventListener('change', handleColorChange);
    colorSelect.addEventListener('change', handleColorChange);
}

function handleColorChange(e) {
    const colorSelect = e.target;
    if (colorSelect.value === '_new_') {
        setTimeout(() => {
            const newColor = prompt('Enter new color:');
            if (newColor && newColor.trim()) {
                const trimmedColor = newColor.trim();

                const option = document.createElement('option');
                option.value = trimmedColor;
                option.textContent = trimmedColor;

                const separator = colorSelect.querySelector('option[disabled]');
                colorSelect.insertBefore(option, separator);
                colorSelect.value = trimmedColor;
            } else {
                colorSelect.value = '';
            }
            checkDuplicateTypeColor();
        }, 100);
    } else {
        checkDuplicateTypeColor();
    }
}

// ============================================
// VALIDATION FUNCTIONS
// ============================================

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
            stockWarning = '<br><span style="color:#e74c3c;">⚠ Low Stock Warning: Below 10m threshold</span>';
        } else if (stock === 10) {
            stockWarning = '<br><span style="color:#f39c12;">⚠ Critical Stock: At 10m threshold</span>';
        }

        detailsDiv.innerHTML = `
            <strong>Fabric ID:</strong> ${select.value}<br>
            <strong>Type:</strong> ${selectedOption.dataset.type}<br>
            <strong>Color:</strong> ${selectedOption.dataset.color}<br>
            <strong>Current Stock:</strong> ${stock.toFixed(2)} meters${stockWarning}
        `;

        const currentStatus = statusSelect.value;
        statusSelect.innerHTML = '<option value="">Select Type</option><option value="In">Stock In</option>';

        if (stock > 10) {
            statusSelect.innerHTML += '<option value="Out">Stock Out</option>';
        }

        statusSelect.value = currentStatus;
        validateMovementType();
    } else {
        detailsDiv.innerHTML = 'Select a fabric to see details';
        selectedFabricStock = 0;
        statusSelect.innerHTML = '<option value="">Select Type</option><option value="In">Stock In</option><option value="Out">Stock Out</option>';
    }
}

function validateMovementType() {
    const statusSelect = document.getElementById('movementStatus');
    const helpText = document.getElementById('movementTypeHelp');

    if (!statusSelect || !helpText) return;

    if (statusSelect.value === 'Out' && selectedFabricStock <= 10) {
        helpText.textContent = 'Cannot stock out - fabric is at or below 10m threshold.';
        helpText.style.color = '#e74c3c';
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
        errorText.textContent = 'Quantity must be greater than 0';
        errorText.style.display = 'block';
        input.classList.add('input-error');
        return false;
    }

    if (statusSelect.value === 'Out' && quantity > selectedFabricStock) {
        helpText.style.display = 'none';
        errorText.textContent = 'Quantity exceeds available stock!';
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
        showNotification('Cannot stock out - fabric is at or below 10m threshold.', 'error');
        return false;
    }

    if (statusSelect.value === 'Out' && quantity > selectedFabricStock) {
        showNotification(`Quantity (${quantity}m) exceeds available stock (${selectedFabricStock}m)!`, 'error');
        quantityInput?.focus();
        return false;
    }

    return true;
}

function validateFabricForm() {
    const typeSelect = document.getElementById('newFabricType');
    const colorSelect = document.getElementById('newFabricColor');
    const initialStock = document.getElementById('initialStock');
    const fabricId = document.getElementById('newFabricId');

    // Validate Fabric ID
    if (!fabricId.value || !fabricId.value.trim()) {
        showNotification('Please enter a Fabric ID', 'error');
        fabricId.focus();
        return false;
    }

    if (!fabricId.value.match(/^FAB\d{3}$/i)) {
        showNotification('Fabric ID must be in format FAB001 (FAB followed by 3 digits)', 'error');
        fabricId.focus();
        return false;
    }

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

    // Fix stock validation
    const stockValue = initialStock.value.trim();
    const stock = parseFloat(stockValue);

    if (isNaN(stock) || stockValue === '') {
        showNotification('Please enter a valid number for initial stock', 'error');
        initialStock.focus();
        return false;
    }

    if (stock < 10) {
        showNotification('Initial stock must be at least 10 meters', 'error');
        initialStock.focus();
        return false;
    }

    if (checkDuplicateTypeColor()) {
        showNotification('This type/color combination already exists', 'error');
        return false;
    }

    return true;
}

// ============================================
// FORM SUBMISSION HANDLERS
// ============================================

function handleFabricSubmit(e) {
    e.preventDefault();
    console.log('Fabric form submitted');

    if (!validateFabricForm()) {
        console.log('Fabric form validation failed');
        return;
    }

    const form = e.target;
    const fabricId = form.fabricId.value.trim().toUpperCase();
    const fabricType = form.fabricType.value.trim();
    const color = form.color.value.trim();
    const initialStock = parseFloat(form.initialStock.value);

    // Use the exact field names that match the Fabric model
    const data = {
        fabricId: fabricId,
        fabricType: fabricType,
        color: color,
        currentStock: initialStock  // Change from initialStock to currentStock
    };

    console.log('Submitting fabric data:', data);

    const submitBtn = document.getElementById('submitFabricBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Adding...';

    fetch('/api/fabrics/new', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res => {
            console.log('Response status:', res.status);
            if (!res.ok) {
                return res.text().then(text => {
                    throw new Error(`HTTP ${res.status}: ${text}`);
                });
            }
            return res.json();
        })
        .then(resp => {
            console.log('Fabric creation response:', resp);
            if (resp && resp.fabricId) {
                showNotification(`Fabric ${resp.fabricId} added successfully!`, 'success');
                closeFabricModalFn();

                // Update local arrays
                if (!existingTypes.includes(fabricType)) {
                    existingTypes.push(fabricType);
                    existingTypes.sort();
                }
                if (!existingColors.includes(color)) {
                    existingColors.push(color);
                    existingColors.sort();
                }

                refreshAllData();
            } else {
                throw new Error('Invalid response from server');
            }
        })
        .catch(err => {
            console.error('Fabric creation error:', err);
            showNotification('Error: ' + err.message, 'error');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Add Fabric';
        });
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

    const data = {
        fabricId: fabricId,
        status: status,
        movementDate: form.movementDate.value,
        quantity: quantity
    };

    const submitBtn = document.getElementById('submitMovementBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Recording...';
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
            if (resp && resp.movementId) {
                showNotification(`Movement ${resp.movementId} recorded successfully!`, 'success');
                closeMovementModalFn();
                refreshAllData();
            } else {
                showNotification('Error recording movement', 'error');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = 'Record Movement';
                }
            }
        })
        .catch(err => {
            console.error(err);
            showNotification('Server error: ' + err.message, 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Record Movement';
            }
        });
}

function calculateAndDisplayStats() {
    console.log('Calculating stats with:', {
        fabricsCount: fabricsList.length,
        movementsCount: movementsList.length
    });

    // Calculate total fabric types
    const totalTypes = fabricsList.length;

    // Calculate total meters - sum of ALL fabric stocks
    const totalMeters = fabricsList.reduce((sum, f) => {
        const stock = parseFloat(f.currentStock) || 0;
        return sum + stock;
    }, 0);

    const today = new Date().toISOString().split('T')[0];

    // Calculate stock in today from APPROVED movements only
    const todayIn = movementsList
        .filter(m => {
            const matchesDate = m.movementDate === today;
            const isStockIn = m.status === 'In' || m.status === 'IN';
            const isApproved = m.approvalStatus === 'APPROVED';
            return matchesDate && isStockIn && isApproved;
        })
        .reduce((sum, m) => {
            const approvedQty = parseFloat(m.approvedQuantity) || 0;
            return sum + approvedQty;
        }, 0);

    // Calculate stock out today from APPROVED movements only
    const todayOut = movementsList
        .filter(m => {
            const matchesDate = m.movementDate === today;
            const isStockOut = m.status === 'Out' || m.status === 'OUT';
            const isApproved = m.approvalStatus === 'APPROVED';
            return matchesDate && isStockOut && isApproved;
        })
        .reduce((sum, m) => {
            const approvedQty = parseFloat(m.approvedQuantity) || 0;
            return sum + approvedQty;
        }, 0);

    console.log('Final Stats:', {
        totalTypes,
        totalMeters,
        todayIn,
        todayOut,
        fabrics: fabricsList.map(f => ({ id: f.fabricId, stock: f.currentStock }))
    });

    // Update the DOM with calculated stats
    const totalFabricTypesElement = document.getElementById('totalFabricTypes');
    const totalMetersElement = document.getElementById('totalMeters');
    const stockInTodayElement = document.getElementById('stockInToday');
    const stockOutTodayElement = document.getElementById('stockOutToday');

    if (totalFabricTypesElement) totalFabricTypesElement.textContent = totalTypes;
    if (totalMetersElement) totalMetersElement.textContent = totalMeters.toFixed(2);
    if (stockInTodayElement) stockInTodayElement.textContent = todayIn.toFixed(2);
    if (stockOutTodayElement) stockOutTodayElement.textContent = todayOut.toFixed(2);
}

function handleApprovalSubmit(e) {
    e.preventDefault();
    console.log('Approval form submitted');

    if (!validateApprovalForm()) {
        console.log('Approval form validation failed');
        return;
    }

    const form = e.target;
    const movementId = form.movementId.value;
    const fabricId = document.getElementById('approvalFabricId').value;
    const approvedQuantity = parseFloat(form.approvedQuantity.value);
    const rejectedQuantity = parseFloat(form.rejectedQuantity.value) || 0;
    const rejectReason = form.rejectReason.value.trim();

    // Create the data object with exact field names expected by backend
    const data = {
        movementId: movementId,
        fabricId: fabricId,
        approvedQuantity: approvedQuantity,
        rejectedQuantity: rejectedQuantity,
        rejectionReason: rejectReason || null
    };

    console.log('Submitting approval data:', data);

    const submitBtn = document.getElementById('submitApprovalBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Submitting...';

    fetch('/api/fabrics/movements/approve', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(async res => {
            console.log('Approval response status:', res.status);
            const responseText = await res.text();

            if (!res.ok) {
                // Try to parse error message from response
                let errorMessage = `HTTP ${res.status}`;
                try {
                    const errorJson = JSON.parse(responseText);
                    errorMessage = errorJson.message || errorJson.error || responseText;
                } catch {
                    errorMessage = responseText || `HTTP ${res.status}`;
                }
                throw new Error(errorMessage);
            }

            try {
                return JSON.parse(responseText);
            } catch (parseError) {
                console.log('Response is not JSON, treating as success');
                return { success: true, message: responseText };
            }
        })
        .then(resp => {
            console.log('Approval response:', resp);
            showNotification('Movement approved successfully! Stock updated.', 'success');
            closeApprovalModalFn();
            refreshAllData();
        })
        .catch(err => {
            console.error('Approval error:', err);
            showNotification('Error: ' + err.message, 'error');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Submit Decision';
        });
}


function validateApprovalForm() {
    const approvedQtyInput = document.getElementById('approvedQuantity');
    const rejectedQtyInput = document.getElementById('rejectedQuantity');
    const totalQtyInput = document.getElementById('approvalTotalQuantity');

    const approvedQty = parseFloat(approvedQtyInput.value) || 0;
    const rejectedQty = parseFloat(rejectedQtyInput.value) || 0;
    const totalQty = parseFloat(totalQtyInput.value) || 0;

    // Reset styles
    approvedQtyInput.classList.remove('input-error');
    rejectedQtyInput.classList.remove('input-error');

    // Validate approved quantity
    if (isNaN(approvedQty) || approvedQty < 0) {
        showNotification('Please enter a valid approved quantity', 'error');
        approvedQtyInput.focus();
        approvedQtyInput.classList.add('input-error');
        return false;
    }

    // Validate rejected quantity
    if (isNaN(rejectedQty) || rejectedQty < 0) {
        showNotification('Please enter a valid rejected quantity', 'error');
        rejectedQtyInput.focus();
        rejectedQtyInput.classList.add('input-error');
        return false;
    }

    // Validate total matches
    const calculatedTotal = approvedQty + rejectedQty;
    if (Math.abs(calculatedTotal - totalQty) > 0.01) { // Allow small floating point differences
        showNotification(`Approved (${approvedQty.toFixed(2)}) + Rejected (${rejectedQty.toFixed(2)}) = ${calculatedTotal.toFixed(2)}m, but must equal Total Quantity (${totalQty.toFixed(2)}m)`, 'error');
        approvedQtyInput.classList.add('input-error');
        rejectedQtyInput.classList.add('input-error');
        return false;
    }

    // Validate at least some quantity is approved or rejected
    if (approvedQty === 0 && rejectedQty === 0) {
        showNotification('Either approved or rejected quantity must be greater than 0', 'error');
        return false;
    }

    return true;
}

// ============================================
// ACTION HANDLERS
// ============================================

function handleActions(e) {
    const deleteFabricBtn = e.target.closest('.btn-delete-fabric');
    const deleteMovementBtn = e.target.closest('.btn-delete-movement');
    const approveBtn = e.target.closest('.approve-btn');

    if (deleteFabricBtn) {
        const id = deleteFabricBtn.dataset.id;
        showConfirmDialog(
            `Are you sure you want to delete fabric <strong>${id}</strong>?<br><br>This will also delete all associated movements and cannot be undone.`,
            () => {
                deleteFabricBtn.disabled = true;
                deleteFabricBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';

                fetch(`/api/fabrics/delete/${id}`, {
                    method: 'DELETE'
                })
                    .then(res => {
                        console.log('Delete fabric response status:', res.status);
                        if (res.status === 200) {
                            return res.text(); // Handle text response
                        } else if (res.status === 404) {
                            throw new Error('Fabric not found');
                        } else {
                            throw new Error(`HTTP error! Status: ${res.status}`);
                        }
                    })
                    .then(responseText => {
                        console.log('Delete fabric response:', responseText);
                        showNotification('Fabric deleted successfully', 'success');
                        refreshAllData();
                    })
                    .catch(err => {
                        console.error('Delete fabric error:', err);
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
                deleteMovementBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';

                fetch(`/api/fabrics/movements/delete/${id}`, {
                    method: 'DELETE'
                })
                    .then(res => {
                        console.log('Delete movement response status:', res.status);
                        if (res.status === 200) {
                            return res.text(); // Handle text response
                        } else if (res.status === 404) {
                            throw new Error('Movement not found');
                        } else {
                            throw new Error(`HTTP error! Status: ${res.status}`);
                        }
                    })
                    .then(responseText => {
                        console.log('Delete movement response:', responseText);
                        showNotification('Movement deleted successfully', 'success');
                        refreshAllData();
                    })
                    .catch(err => {
                        console.error('Delete movement error:', err);
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
        const quantity = parseFloat(approveBtn.dataset.quantity);

        document.getElementById('approvalMovementId').value = movementId;
        document.getElementById('approvalFabricId').value = fabricId;
        document.getElementById('approvalTotalQuantity').value = quantity.toFixed(2);
        document.getElementById('approvedQuantity').value = quantity.toFixed(2);
        document.getElementById('rejectedQuantity').value = '0.00';
        document.getElementById('rejectReason').value = '';
        document.getElementById('rejectReasonGroup').style.display = 'none';
        document.getElementById('movementApprovalModal').style.display = 'block';
        setTodayDate();
    }
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

function refreshAllData() {
    console.log('Refreshing all data...');

    // Use Promise.all to ensure both datasets are loaded before calculating stats
    Promise.all([
        loadFabricsAsync(),
        loadMovementsAsync()
    ]).then(() => {
        console.log('Both datasets refreshed, updating stats...');
        calculateAndDisplayStats();
    }).catch(err => {
        console.error('Error refreshing data:', err);
        // Still calculate stats with available data
        calculateAndDisplayStats();
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
                <h3 class="modal-title"><i class="fas fa-exclamation-triangle"></i> Confirm Action</h3>
            </div>
            <div style="padding:25px;">
                <p style="margin-bottom:20px;color:#555;line-height:1.6;">${message}</p>
                <div class="form-buttons">
                    <button type="button" class="btn btn-cancel" id="confirmCancel">Cancel</button>
                    <button type="button" class="btn delete" id="confirmOk">Confirm</button>
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