// hrDashboard_Payroll.js - FIXED VERSION
// Payroll Management Functionality

let currentPayrollData = [];
let currentPayrollMonth = '';
let allPayrollData = []; // Store all payroll data

// Initialize payroll when section is shown
function initPayroll() {
    console.log('=== INITIALIZING PAYROLL SECTION ===');

    const payrollMonthElement = document.getElementById('payrollMonth');
    const payrollTableElement = document.getElementById('payrollTable');

    if (!payrollMonthElement || !payrollTableElement) {
        console.error('Payroll elements not found, retrying...');
        setTimeout(initPayroll, 500);
        return;
    }

    // Set current month as default
    const now = new Date();
    const currentMonth = now.toISOString().slice(0, 7);
    payrollMonthElement.value = currentMonth;
    currentPayrollMonth = currentMonth;

    console.log('Current month set to:', currentMonth);

    // Load ALL payroll data first to check what exists
    loadAllPayrollData();
}

// Load ALL payroll records (for checking existing data)
async function loadAllPayrollData() {
    try {
        console.log('Loading all payroll records...');
        const response = await fetchWithAuth('/payroll/all');

        if (response.ok) {
            const result = await response.json();

            if (result.success && result.payrolls) {
                allPayrollData = result.payrolls;
                console.log(`Found ${allPayrollData.length} total payroll records`);

                // Get unique months
                const months = [...new Set(allPayrollData.map(p => p.payroll.payrollMonth))];
                console.log('Available months:', months);

                // Now load data for selected month
                await loadPayrollData();
            } else {
                console.log('No existing payroll data found');
                showNoDataMessage();
            }
        } else {
            console.warn('Could not fetch all payroll data');
            // Still try to load current month data
            await loadPayrollData();
        }
    } catch (error) {
        console.error('Error loading all payroll data:', error);
        // Continue anyway and try to load current month
        await loadPayrollData();
    }
}

// Load payroll data for selected month
async function loadPayrollData() {
    const monthElement = document.getElementById('payrollMonth');
    const statusElement = document.getElementById('payrollStatus');

    if (!monthElement) {
        console.error('Month element not found');
        return;
    }

    const month = monthElement.value;
    const status = statusElement ? statusElement.value : 'ALL';

    if (!month) {
        showAlert('Please select a month', 'error');
        return;
    }

    try {
        console.log(`=== LOADING PAYROLL DATA FOR ${month} ===`);

        const [year, monthNum] = month.split('-');
        const response = await fetchWithAuth(`/payroll/monthly-report?year=${year}&month=${monthNum}`);

        if (response.ok) {
            const result = await response.json();
            console.log('Payroll response:', result);

            if (result.success && result.payrolls && result.payrolls.length > 0) {
                // Store ALL data first
                currentPayrollData = result.payrolls;
                currentPayrollMonth = month;

                console.log(`✓ Loaded ${currentPayrollData.length} payroll records`);

                // Apply status filter if needed (but keep original data)
                let dataToDisplay = currentPayrollData;
                if (status !== 'ALL') {
                    dataToDisplay = currentPayrollData.filter(item =>
                        item.payroll.status === status
                    );
                    console.log(`Filtered to ${dataToDisplay.length} records with status ${status}`);
                }

                displayPayrollData({ payrolls: dataToDisplay });
                updatePayrollSummary(result);

                // Show table and summary
                document.getElementById('noPayrollData').style.display = 'none';
                document.getElementById('payrollSummary').style.display = 'block';
                document.querySelector('.table-container').style.display = 'block';

                // Clear search box
                const searchBox = document.getElementById('payrollEmployee');
                if (searchBox && searchBox.value) {
                    console.log('Search term detected, applying filter...');
                    filterPayrollTable();
                }

            } else {
                console.log('No payroll data for selected month');
                currentPayrollData = [];
                showNoDataMessage();
            }
        } else {
            console.log('Server returned error:', response.status);
            currentPayrollData = [];
            showNoDataMessage();
        }
    } catch (error) {
        console.error('Error loading payroll:', error);
        showAlert('Error loading payroll: ' + error.message, 'error');
        showNoDataMessage();
    }
}


// Display payroll data in table
function displayPayrollData(result) {
    const tbody = document.querySelector('#payrollTable tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    const dataToDisplay = result.payrolls || [];

    if (dataToDisplay.length === 0) {
        tbody.innerHTML = '<tr><td colspan="12" style="text-align: center; padding: 20px;">No payroll records found</td></tr>';
        return;
    }

    console.log(`Displaying ${dataToDisplay.length} payroll records`);

    dataToDisplay.forEach(item => {
        const p = item.payroll;
        const row = document.createElement('tr');

        // Determine which buttons to show based on status
        let actionButtons = '';

        if (p.status === 'PAID') {
            // PAID status: Only show View button
            actionButtons = `
                <button class="btn btn-info btn-sm" onclick="viewPayrollDetails('${p.employeeId}', '${p.payrollMonth}')" title="View Details">
                    <i class="fas fa-eye"></i> View
                </button>
            `;
        } else if (p.status === 'CALCULATED') {
            // CALCULATED status: Show View, Edit, and Pay buttons
            actionButtons = `
                <button class="btn btn-info btn-sm" onclick="viewPayrollDetails('${p.employeeId}', '${p.payrollMonth}')" title="View Details">
                    <i class="fas fa-eye"></i> View
                </button>
                <button class="btn btn-warning btn-sm" onclick="editOtRate('${p.employeeId}', '${p.payrollMonth}', ${p.otRate})" title="Edit OT Rate">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn btn-success btn-sm" onclick="showMarkAsPaidModal('${p.employeeId}', '${p.payrollMonth}', '${item.employeeName}')" title="Mark as Paid">
                    <i class="fas fa-check"></i> Pay
                </button>
            `;
        } else {
            // PENDING or other status: Show View and Edit only
            actionButtons = `
                <button class="btn btn-info btn-sm" onclick="viewPayrollDetails('${p.employeeId}', '${p.payrollMonth}')" title="View Details">
                    <i class="fas fa-eye"></i> View
                </button>
                <button class="btn btn-warning btn-sm" onclick="editOtRate('${p.employeeId}', '${p.payrollMonth}', ${p.otRate})" title="Edit OT Rate">
                    <i class="fas fa-edit"></i> Edit
                </button>
            `;
        }

        row.innerHTML = `
            <td>${p.employeeId}</td>
            <td>${item.employeeName || 'N/A'}</td>
            <td>$${formatCurrency(p.basicSalary)}</td>
            <td>${formatNumber(p.actualWorkHours)}h</td>
            <td>${formatNumber(p.otHours)}h</td>
            <td>$${formatCurrency(p.otAmount)}</td>
            <td>$${formatCurrency(p.grossSalary)}</td>
            <td>$${formatCurrency(p.deductions)}</td>
            <td><strong>$${formatCurrency(p.netSalary)}</strong></td>
            <td>${formatNumber(p.attendanceRate)}%</td>
            <td><span class="status-badge status-badge-${p.status.toLowerCase()}">${p.status}</span></td>
            <td>
                <div class="action-buttons-payroll">
                    ${actionButtons}
                </div>
            </td>
        `;

        tbody.appendChild(row);
    });

    console.log('✓ Table populated successfully');
}

// NEW: Show Mark as Paid Modal instead of confirm()
function showMarkAsPaidModal(employeeId, payrollMonth, employeeName) {
    // Find the payroll data for this employee
    const payrollItem = currentPayrollData.find(item =>
        item.payroll.employeeId === employeeId &&
        item.payroll.payrollMonth === payrollMonth
    );

    if (!payrollItem) {
        showAlert('Payroll data not found', 'error');
        return;
    }

    const p = payrollItem.payroll;

    // Populate modal content
    document.getElementById('markPaidEmployeeId').value = employeeId;
    document.getElementById('markPaidPayrollMonth').value = payrollMonth;

    document.getElementById('markPaidEmployeeDetails').innerHTML = `
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
            <div>
                <strong>Employee:</strong><br>
                ${employeeName} (${employeeId})
            </div>
            <div>
                <strong>Month:</strong><br>
                ${formatMonthYear(payrollMonth)}
            </div>
            <div>
                <strong>Gross Salary:</strong><br>
                <span style="font-size: 18px; color: #333;">$${formatCurrency(p.grossSalary)}</span>
            </div>
            <div>
                <strong>Deductions:</strong><br>
                <span style="font-size: 18px; color: #dc3545;">-$${formatCurrency(p.deductions)}</span>
            </div>
            <div style="grid-column: 1 / -1; padding: 10px; background: #e7f3ff; border-radius: 4px; border-left: 4px solid #2196F3;">
                <strong>Net Salary to be Paid:</strong><br>
                <span style="font-size: 24px; color: #28a745; font-weight: bold;">$${formatCurrency(p.netSalary)}</span>
            </div>
        </div>
    `;

    // Show modal
    document.getElementById('markAsPaidModal').style.display = 'block';
}

// Close Mark as Paid Modal
function closeMarkAsPaidModal() {
    document.getElementById('markAsPaidModal').style.display = 'none';
    document.getElementById('paymentNotes').value = ''; // Clear notes
}

// Handle Mark as Paid form submission
// Handle Mark as Paid form submission - FIXED VERSION
const markPaidForm = document.getElementById('markAsPaidForm');
if (markPaidForm) {
    markPaidForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const employeeId = document.getElementById('markPaidEmployeeId').value;
        const payrollMonth = document.getElementById('markPaidPayrollMonth').value;

        try {
            // Step 1: Close the modal
            closeMarkAsPaidModal();

            // Step 2: Show processing overlay
            showProcessingOverlay('Processing payment for ' + employeeId + '...');

            // Wait 1 second to show processing state
            await new Promise(resolve => setTimeout(resolve, 1000));

            // Step 3: Make API call
            const response = await fetchWithAuth(
                `/payroll/mark-paid?employeeId=${employeeId}&payrollMonth=${payrollMonth}`,
                { method: 'PUT' }
            );

            const result = await response.json();

            if (result.success) {
                // Step 4: Show success state for 2 seconds
                showProcessingSuccess();

                // Step 5: Wait 2 seconds
                await new Promise(resolve => setTimeout(resolve, 2000));

                // Step 6: Hide overlay
                hideProcessingOverlay();

                // Step 7: Show success alert
                showAlert(`✓ Payroll marked as PAID for ${employeeId}`, 'success');

                // Step 8: Reload table data
                await loadPayrollData();

                // Step 9: Highlight the updated row
                highlightPayrollRow(employeeId);

            } else {
                // Hide overlay and show error
                hideProcessingOverlay();
                showAlert(result.message || 'Failed to mark as paid', 'error');
            }

        } catch (error) {
            console.error('Error:', error);
            hideProcessingOverlay();
            showAlert('Error marking as paid: ' + error.message, 'error');
        }
    });
}

// Highlight the updated row after payment
function highlightPayrollRow(employeeId) {
    setTimeout(() => {
        const rows = document.querySelectorAll('#payrollTable tbody tr');
        rows.forEach(row => {
            const empIdCell = row.cells[0];
            if (empIdCell && empIdCell.textContent === employeeId) {
                row.style.backgroundColor = '#d4edda';
                row.style.transition = 'background-color 0.5s';

                // Remove highlight after 3 seconds
                setTimeout(() => {
                    row.style.backgroundColor = '';
                }, 3000);
            }
        });
    }, 100);
}



// Update payroll summary
function updatePayrollSummary(result) {
    document.getElementById('summaryMonth').textContent = formatMonthYear(currentPayrollMonth);
    document.getElementById('summaryEmployeeCount').textContent = result.employeeCount || 0;
    document.getElementById('summaryGrossSalary').textContent = `$${formatCurrency(result.totalGrossSalary || 0)}`;
    document.getElementById('summaryDeductions').textContent = `$${formatCurrency(result.totalDeductions || 0)}`;
    document.getElementById('summaryNetSalary').textContent = `$${formatCurrency(result.totalNetSalary || 0)}`;

    const allPaid = currentPayrollData.every(item => item.payroll.status === 'PAID');
    const anyCalculated = currentPayrollData.some(item => item.payroll.status === 'CALCULATED');

    let status = allPaid ? 'COMPLETED' : anyCalculated ? 'READY FOR PAYMENT' : 'PENDING';
    document.getElementById('summaryStatus').textContent = status;
}

// FIXED: Employee filter function
// Update the filter function to use the new display logic
function filterPayrollTable() {
    const searchInput = document.getElementById('payrollEmployee');
    const statusSelect = document.getElementById('payrollStatus');

    if (!searchInput) {
        console.error('Search input not found');
        return;
    }

    const searchTerm = searchInput.value.toLowerCase().trim();
    const statusFilter = statusSelect ? statusSelect.value : 'ALL';

    console.log('=== FILTERING PAYROLL TABLE ===');
    console.log('Search term:', searchTerm);
    console.log('Status filter:', statusFilter);

    // If no filters, show all data
    if (searchTerm === '' && statusFilter === 'ALL') {
        displayPayrollData({ payrolls: currentPayrollData });
        return;
    }

    // Filter the data
    const filteredData = currentPayrollData.filter(item => {
        const p = item.payroll;
        const employeeId = (p.employeeId || '').toLowerCase();
        const employeeName = (item.employeeName || '').toLowerCase();
        const status = p.status || '';

        const matchesSearch = searchTerm === '' ||
            employeeId.includes(searchTerm) ||
            employeeName.includes(searchTerm);

        const matchesStatus = statusFilter === 'ALL' || status === statusFilter;

        return matchesSearch && matchesStatus;
    });

    console.log('Filtered records:', filteredData.length);

    // Display filtered data
    displayPayrollData({ payrolls: filteredData });
}

// Generate payroll modal
function showGeneratePayrollModal() {
    const now = new Date();
    const currentMonth = now.toISOString().slice(0, 7);
    document.getElementById('generateMonth').value = currentMonth;
    document.getElementById('generatePayrollModal').style.display = 'block';
}

function closeGeneratePayrollModal() {
    document.getElementById('generatePayrollModal').style.display = 'none';
}

// Generate payroll form submission
const generateForm = document.getElementById('generatePayrollForm');
if (generateForm) {
    generateForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const month = document.getElementById('generateMonth').value;
        if (!month) {
            showAlert('Please select a month', 'error');
            return;
        }

        const [year, monthNum] = month.split('-');

        console.log('=== STARTING PAYROLL GENERATION ===');
        console.log('Year:', year, 'Month:', monthNum);
        console.log('Month format:', month);

        try {
            // Show loading state
            const submitBtn = this.querySelector('button[type="submit"]');
            const originalBtnText = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';

            // STEP 1: Calculate attendance summary
            console.log('Step 1: Calculating attendance summary...');
            console.log('API URL:', `/attendance-summary/calculate?year=${year}&month=${monthNum}`);

            const summaryResp = await fetchWithAuth(
                `/attendance-summary/calculate?year=${year}&month=${monthNum}`,
                { method: 'POST' }
            );

            console.log('Summary response status:', summaryResp.status);

            // Get response text first for debugging
            const summaryText = await summaryResp.text();
            console.log('Summary raw response:', summaryText);

            let summaryResult;
            try {
                summaryResult = JSON.parse(summaryText);
                console.log('Summary parsed result:', summaryResult);
            } catch (parseError) {
                console.error('Failed to parse summary response:', parseError);
                showAlert('Server returned invalid response for attendance calculation', 'error');
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnText;
                return;
            }

            // Check if attendance summary calculation was successful
            if (!summaryResult || !summaryResult.success) {
                const errorMsg = summaryResult?.message || 'No attendance data found for this month';
                console.error('Attendance calculation failed:', errorMsg);

                // Show detailed error
                showAlert(
                    `Cannot generate payroll: ${errorMsg}\n\n` +
                    `Please ensure:\n` +
                    `1. Attendance has been recorded for ${month}\n` +
                    `2. Employees have checked in/out during this month\n` +
                    `3. The month you selected has attendance data`,
                    'error'
                );

                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnText;
                return;
            }

            console.log('✓ Attendance summary calculated successfully');
            console.log('Summary details:', summaryResult);

            // STEP 2: Generate payroll
            console.log('Step 2: Generating payroll...');
            console.log('API URL:', `/payroll/generate?year=${year}&month=${monthNum}`);

            const payrollResp = await fetchWithAuth(
                `/payroll/generate?year=${year}&month=${monthNum}`,
                { method: 'POST' }
            );

            console.log('Payroll response status:', payrollResp.status);

            const payrollText = await payrollResp.text();
            console.log('Payroll raw response:', payrollText);

            let payrollResult;
            try {
                payrollResult = JSON.parse(payrollText);
                console.log('Payroll parsed result:', payrollResult);
            } catch (parseError) {
                console.error('Failed to parse payroll response:', parseError);
                showAlert('Server returned invalid response for payroll generation', 'error');
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnText;
                return;
            }

            // Check payroll generation result
            if (payrollResult.success) {
                const count = payrollResult.generatedPayrolls?.length || 0;
                const errors = payrollResult.errors?.length || 0;

                let message = `✓ Payroll generated successfully!\n\n`;
                message += `Generated: ${count} employees\n`;

                if (errors > 0) {
                    message += `Errors: ${errors} employees\n\n`;
                    message += 'Some employees had issues:\n';
                    payrollResult.errors.slice(0, 3).forEach(err => {
                        message += `• ${err}\n`;
                    });
                    if (errors > 3) {
                        message += `... and ${errors - 3} more`;
                    }
                }

                showAlert(message, 'success');

                closeGeneratePayrollModal();

                // Reload data
                document.getElementById('payrollMonth').value = month;
                await loadAllPayrollData();

            } else {
                const errorMsg = payrollResult.message || 'Failed to generate payroll';
                console.error('Payroll generation failed:', errorMsg);
                showAlert(`Payroll generation failed: ${errorMsg}`, 'error');
            }

            // Reset button
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalBtnText;

        } catch (error) {
            console.error('=== ERROR IN PAYROLL GENERATION ===');
            console.error('Error type:', error.name);
            console.error('Error message:', error.message);
            console.error('Error stack:', error.stack);

            showAlert(
                `Error generating payroll: ${error.message}\n\n` +
                `Please check:\n` +
                `1. You are logged in\n` +
                `2. The server is running\n` +
                `3. Attendance data exists for this month`,
                'error'
            );

            // Reset button
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnText;
            }
        }
    });
}

// View payroll details
async function viewPayrollDetails(employeeId, payrollMonth) {
    try {
        const [year, month] = payrollMonth.split('-');
        const response = await fetchWithAuth(
            `/payroll/employee?employeeId=${employeeId}&year=${year}&month=${month}`
        );
        const result = await response.json();

        if (result.success) {
            displayPayrollDetails(result);
        } else {
            showAlert('Failed to load details', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('Error loading details: ' + error.message, 'error');
    }
}

// Display payroll details modal
function displayPayrollDetails(result) {
    const p = result.payroll;
    const hourlyRate = (p.basicSalary / p.maxWorkHours).toFixed(2);
    const regularPay = (p.grossSalary - p.otAmount).toFixed(2);

    document.getElementById('payrollDetailsContent').innerHTML = `
        <div class="payroll-details-section">
            <h4>Employee Information</h4>
            <div class="payroll-details-grid">
                <div><span>Employee:</span><span><strong>${result.employeeName}</strong> (${p.employeeId})</span></div>
                <div><span>Month:</span><span>${formatMonthYear(p.payrollMonth)}</span></div>
                <div><span>Status:</span><span class="status-badge status-badge-${p.status.toLowerCase()}">${p.status}</span></div>
            </div>
        </div>

        <div class="payroll-details-section">
            <h4>Work Summary</h4>
            <div class="payroll-details-grid">
                <div><span>Actual Hours:</span><span>${formatNumber(p.actualWorkHours)}h</span></div>
                <div><span>Max Regular Hours:</span><span>${formatNumber(p.maxWorkHours)}h</span></div>
                <div><span>Overtime Hours:</span><span>${formatNumber(p.otHours)}h</span></div>
                <div><span>Attendance Rate:</span><span>${formatNumber(p.attendanceRate)}%</span></div>
            </div>
        </div>

        <div class="payroll-details-section">
            <h4>Salary Breakdown</h4>
            <div class="payroll-breakdown">
                <div><span>Monthly Salary (Base):</span><span>$${formatCurrency(p.basicSalary)}</span></div>
                <div><span>Hourly Rate:</span><span>$${hourlyRate}/hour</span></div>
                <div><span>Regular Pay (${Math.min(p.actualWorkHours, p.maxWorkHours).toFixed(1)}h):</span><span>$${regularPay}</span></div>
                <div><span>OT Multiplier:</span><span>${p.otRate}x</span></div>
                <div><span>OT Pay (${p.otHours}h × ${p.otRate}x):</span><span>$${formatCurrency(p.otAmount)}</span></div>
                <div><span><strong>Gross Salary:</strong></span><span><strong>$${formatCurrency(p.grossSalary)}</strong></span></div>
                <div><span>Deductions (EPF/ETF):</span><span class="deduction">-$${formatCurrency(p.deductions)}</span></div>
                <div style="border-top: 2px solid #000; margin-top: 8px; padding-top: 8px;">
                    <span><strong>Net Salary:</strong></span>
                    <span><strong style="color: #28a745;">$${formatCurrency(p.netSalary)}</strong></span>
                </div>
            </div>
        </div>
    `;

    document.getElementById('payrollDetailsModal').style.display = 'block';
}

function closePayrollDetailsModal() {
    document.getElementById('payrollDetailsModal').style.display = 'none';
}

// Edit OT Rate (actually OT multiplier)
function editOtRate(employeeId, payrollMonth, currentRate) {
    const employee = currentPayrollData.find(item => item.payroll.employeeId === employeeId);
    const employeeName = employee ? employee.employeeName : 'Unknown';

    document.getElementById('editOtEmployeeId').value = employeeId;
    document.getElementById('editOtPayrollMonth').value = payrollMonth;
    document.getElementById('editOtRate').value = currentRate;

    document.getElementById('editOtEmployeeDetails').innerHTML = `
        <strong>Employee:</strong> ${employeeName} (${employeeId})<br>
        <strong>Month:</strong> ${formatMonthYear(payrollMonth)}<br>
        <strong>Current OT Multiplier:</strong> ${currentRate}x<br>
        <small style="color: #666;">This multiplier is applied to the hourly rate for overtime hours.</small>
    `;

    document.getElementById('editOtRateModal').style.display = 'block';
}

function closeEditOtRateModal() {
    document.getElementById('editOtRateModal').style.display = 'none';
}

// Update OT rate
const editOtForm = document.getElementById('editOtRateForm');
if (editOtForm) {
    editOtForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const employeeId = document.getElementById('editOtEmployeeId').value;
        const payrollMonth = document.getElementById('editOtPayrollMonth').value;
        const otRate = document.getElementById('editOtRate').value;

        try {
            const response = await fetchWithAuth(
                `/payroll/ot-rate?employeeId=${employeeId}&payrollMonth=${payrollMonth}&otRate=${otRate}`,
                { method: 'PUT' }
            );
            const result = await response.json();

            if (result.success) {
                showAlert('OT multiplier updated!', 'success');
                closeEditOtRateModal();
                loadPayrollData();
            } else {
                showAlert(result.message || 'Update failed', 'error');
            }
        } catch (error) {
            console.error('Error:', error);
            showAlert('Error updating OT multiplier: ' + error.message, 'error');
        }
    });
}

// Mark as paid
async function markAsPaid(employeeId, payrollMonth) {
    if (!confirm(`Mark payroll as PAID for ${employeeId}? This cannot be undone.`)) return;

    try {
        const response = await fetchWithAuth(
            `/payroll/mark-paid?employeeId=${employeeId}&payrollMonth=${payrollMonth}`,
            { method: 'PUT' }
        );
        const result = await response.json();

        if (result.success) {
            showAlert('Marked as paid!', 'success');
            loadPayrollData();
        } else {
            showAlert(result.message || 'Failed', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('Error: ' + error.message, 'error');
    }
}

// Export report
async function exportPayrollReport() {
    if (currentPayrollData.length === 0) {
        showAlert('No data to export', 'error');
        return;
    }

    let csv = `Payroll Report - ${formatMonthYear(currentPayrollMonth)}\n\n`;
    csv += `Employee ID,Name,Basic Salary,Hours,OT Hours,OT Amount,Gross,Deductions,Net,Attendance%,Status\n`;

    currentPayrollData.forEach(item => {
        const p = item.payroll;
        csv += `${p.employeeId},"${item.employeeName}",${p.basicSalary},${p.actualWorkHours},${p.otHours},${p.otAmount},${p.grossSalary},${p.deductions},${p.netSalary},${p.attendanceRate},${p.status}\n`;
    });

    const blob = new Blob([csv], { type: 'text/csv' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `payroll-${currentPayrollMonth}.csv`;
    link.click();

    showAlert('Report exported!', 'success');
}

// Utility functions
function formatCurrency(amt) {
    return parseFloat(amt).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
}

function formatNumber(num) {
    return parseFloat(num).toFixed(2);
}

function formatMonthYear(monthYear) {
    const [year, month] = monthYear.split('-');
    return new Date(year, month - 1).toLocaleDateString('en-US', { year: 'numeric', month: 'long' });
}

function showNoDataMessage() {
    document.getElementById('noPayrollData').style.display = 'block';
    document.getElementById('payrollSummary').style.display = 'none';
    document.querySelector('.table-container').style.display = 'none';
}

// Show processing overlay
function showProcessingOverlay(message = 'Processing Payment...') {
    const overlay = document.getElementById('processingOverlay');
    const messageEl = document.getElementById('processingMessage');
    if (overlay) {
        messageEl.textContent = message;
        overlay.style.display = 'flex';
    }
}

// Hide processing overlay
function hideProcessingOverlay() {
    const overlay = document.getElementById('processingOverlay');
    if (overlay) {
        overlay.style.display = 'none';
    }
}

// Show success state in overlay
function showProcessingSuccess() {
    const overlay = document.getElementById('processingOverlay');
    const content = overlay.querySelector('.processing-content');

    content.innerHTML = `
        <div style="width: 80px; height: 80px; margin: 0 auto 20px; background: #28a745; border-radius: 50%; display: flex; align-items: center; justify-content: center;">
            <i class="fas fa-check" style="font-size: 40px; color: white;"></i>
        </div>
        <h3 style="color: #28a745;">Payment Successful!</h3>
        <p>The payroll has been marked as PAID</p>
    `;
}

// Make functions globally accessible
window.showProcessingOverlay = showProcessingOverlay;
window.hideProcessingOverlay = hideProcessingOverlay;
window.showProcessingSuccess = showProcessingSuccess;

// Export functions globally
window.initPayroll = initPayroll;
window.loadPayrollData = loadPayrollData;
window.highlightPayrollRow = highlightPayrollRow;
window.filterPayrollTable = filterPayrollTable;
window.showGeneratePayrollModal = showGeneratePayrollModal;
window.closeGeneratePayrollModal = closeGeneratePayrollModal;
window.viewPayrollDetails = viewPayrollDetails;
window.closePayrollDetailsModal = closePayrollDetailsModal;
window.editOtRate = editOtRate;
window.closeEditOtRateModal = closeEditOtRateModal;
window.markAsPaid = markAsPaid;
window.exportPayrollReport = exportPayrollReport;
window.showMarkAsPaidModal = showMarkAsPaidModal;
window.closeMarkAsPaidModal = closeMarkAsPaidModal;
window.displayPayrollData = displayPayrollData;