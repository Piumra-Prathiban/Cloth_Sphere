// hrDashboard_Payroll.js
// Payroll Management Functionality

let currentPayrollData = [];
let currentPayrollMonth = '';

// Initialize payroll when section is shown
function initPayroll() {
    console.log('Initializing payroll management...');

    // Check if elements exist before accessing them
    const payrollMonthElement = document.getElementById('payrollMonth');
    const payrollTableElement = document.getElementById('payrollTable');

    if (!payrollMonthElement || !payrollTableElement) {
        console.error('Payroll elements not found in DOM. Retrying...');
        setTimeout(initPayroll, 500);
        return;
    }

    // Set default month to current month
    const now = new Date();
    const currentMonth = now.toISOString().slice(0, 7);
    payrollMonthElement.value = currentMonth;

    // Load ALL payroll data initially (don't filter by month)
    loadAllPayrollData();
}

// Load ALL payroll data (all months)
async function loadAllPayrollData() {
    try {
        // You'll need to create an endpoint that returns all payroll records
        // For now, let's load the selected month
        await loadPayrollData();
    } catch (error) {
        console.error('Error loading all payroll data:', error);
        showAlert('Error loading payroll data: ' + error.message, 'error');
    }
}

// Load payroll data based on filters
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
        showLoading('payrollTable');

        const [year, monthNum] = month.split('-');
        const response = await fetchWithAuth(`/payroll/monthly-report?year=${year}&month=${monthNum}`);

        if (response.ok) {
            const result = await response.json();

            if (result.success) {
                currentPayrollData = result.payrolls || [];
                currentPayrollMonth = month;

                displayPayrollData(result);
                updatePayrollSummary(result);

                // Show/hide no data message
                const noDataDiv = document.getElementById('noPayrollData');
                const summaryDiv = document.getElementById('payrollSummary');
                const tableContainer = document.querySelector('.table-container');

                if (currentPayrollData.length === 0) {
                    if (noDataDiv) noDataDiv.style.display = 'block';
                    if (summaryDiv) summaryDiv.style.display = 'none';
                    if (tableContainer) tableContainer.style.display = 'none';
                } else {
                    if (noDataDiv) noDataDiv.style.display = 'none';
                    if (summaryDiv) summaryDiv.style.display = 'block';
                    if (tableContainer) tableContainer.style.display = 'block';
                }

            } else {
                showAlert(result.message || 'Failed to load payroll data', 'error');
                showNoDataMessage();
            }
        } else {
            throw new Error('Failed to fetch payroll data');
        }
    } catch (error) {
        console.error('Error loading payroll data:', error);
        showAlert('Error loading payroll data: ' + error.message, 'error');
        showNoDataMessage();
    }
}

// Display payroll data in table
function displayPayrollData(result) {
    const tbody = document.querySelector('#payrollTable tbody');
    if (!tbody) {
        console.error('Payroll table body not found');
        return;
    }

    tbody.innerHTML = '';

    if (!currentPayrollData || currentPayrollData.length === 0) {
        tbody.innerHTML = '<tr><td colspan="12" style="text-align: center;">No payroll data available</td></tr>';
        return;
    }

    currentPayrollData.forEach(item => {
        const payroll = item.payroll;
        const row = document.createElement('tr');

        row.innerHTML = `
            <td>${payroll.employeeId}</td>
            <td>${item.employeeName || 'N/A'}</td>
            <td>$${formatCurrency(payroll.basicSalary)}</td>
            <td>${formatNumber(payroll.actualWorkHours)}h</td>
            <td>${formatNumber(payroll.otHours)}h</td>
            <td>$${formatCurrency(payroll.otAmount)}</td>
            <td>$${formatCurrency(payroll.grossSalary)}</td>
            <td>$${formatCurrency(payroll.deductions)}</td>
            <td><strong>$${formatCurrency(payroll.netSalary)}</strong></td>
            <td>${formatNumber(payroll.attendanceRate)}%</td>
            <td><span class="status-badge status-badge-${payroll.status.toLowerCase()}">${payroll.status}</span></td>
            <td>
                <div class="action-buttons-payroll">
                    <button class="btn btn-info btn-sm" onclick="viewPayrollDetails('${payroll.employeeId}', '${payroll.payrollMonth}')" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-warning btn-sm" onclick="editOtRate('${payroll.employeeId}', '${payroll.payrollMonth}', ${payroll.otRate})" title="Edit OT Rate">
                        <i class="fas fa-edit"></i>
                    </button>
                    ${payroll.status === 'CALCULATED' ? `
                    <button class="btn btn-success btn-sm" onclick="markAsPaid('${payroll.employeeId}', '${payroll.payrollMonth}')" title="Mark as Paid">
                        <i class="fas fa-check"></i>
                    </button>
                    ` : ''}
                </div>
            </td>
        `;

        tbody.appendChild(row);
    });
}

// Update payroll summary
function updatePayrollSummary(result) {
    const elements = {
        summaryMonth: document.getElementById('summaryMonth'),
        summaryEmployeeCount: document.getElementById('summaryEmployeeCount'),
        summaryGrossSalary: document.getElementById('summaryGrossSalary'),
        summaryDeductions: document.getElementById('summaryDeductions'),
        summaryNetSalary: document.getElementById('summaryNetSalary'),
        summaryStatus: document.getElementById('summaryStatus')
    };

    // Check if elements exist before setting values
    if (elements.summaryMonth) elements.summaryMonth.textContent = formatMonthYear(currentPayrollMonth);
    if (elements.summaryEmployeeCount) elements.summaryEmployeeCount.textContent = result.employeeCount || 0;
    if (elements.summaryGrossSalary) elements.summaryGrossSalary.textContent = `$${formatCurrency(result.totalGrossSalary || 0)}`;
    if (elements.summaryDeductions) elements.summaryDeductions.textContent = `$${formatCurrency(result.totalDeductions || 0)}`;
    if (elements.summaryNetSalary) elements.summaryNetSalary.textContent = `$${formatCurrency(result.totalNetSalary || 0)}`;

    // Determine overall status
    const allPaid = currentPayrollData.every(item => item.payroll.status === 'PAID');
    const anyCalculated = currentPayrollData.some(item => item.payroll.status === 'CALCULATED');

    let status = 'MIXED';
    if (allPaid) status = 'COMPLETED';
    else if (anyCalculated) status = 'READY FOR PAYMENT';
    else status = 'PENDING CALCULATION';

    if (elements.summaryStatus) elements.summaryStatus.textContent = status;
}

// Filter payroll table
function filterPayrollTable() {
    const searchElement = document.getElementById('payrollEmployee');
    const statusElement = document.getElementById('payrollStatus');

    if (!searchElement) return;

    const searchTerm = searchElement.value.toLowerCase();
    const statusFilter = statusElement ? statusElement.value : 'ALL';

    const tbody = document.querySelector('#payrollTable tbody');
    if (!tbody) return;

    const rows = tbody.getElementsByTagName('tr');

    for (let row of rows) {
        const employeeId = row.cells[0].textContent.toLowerCase();
        const employeeName = row.cells[1].textContent.toLowerCase();
        const status = row.cells[10].textContent;

        const matchesSearch = employeeId.includes(searchTerm) || employeeName.includes(searchTerm);
        const matchesStatus = statusFilter === 'ALL' || status === statusFilter;

        row.style.display = matchesSearch && matchesStatus ? '' : 'none';
    }
}

// Show generate payroll modal
function showGeneratePayrollModal() {
    const now = new Date();
    const currentMonth = now.toISOString().slice(0, 7);
    const generateMonthElement = document.getElementById('generateMonth');

    if (generateMonthElement) {
        generateMonthElement.value = currentMonth;
    }

    const modal = document.getElementById('generatePayrollModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

// Close generate payroll modal
function closeGeneratePayrollModal() {
    const modal = document.getElementById('generatePayrollModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// Generate payroll
const generateFormElement = document.getElementById('generatePayrollForm');
if (generateFormElement) {
    generateFormElement.addEventListener('submit', async function(e) {
        e.preventDefault();

        const month = document.getElementById('generateMonth').value;

        if (!month) {
            showAlert('Please select a month', 'error');
            return;
        }

        try {
            const [year, monthNum] = month.split('-');

            showLoading('generatePayrollModal');

            console.log('=== STARTING PAYROLL GENERATION PROCESS ===');
            console.log('Step 1: Calculating attendance summary...');

            // STEP 1: Calculate attendance summary first
            const summaryResponse = await fetchWithAuth(`/attendance-summary/calculate?year=${year}&month=${monthNum}`, {
                method: 'POST'
            });

            const summaryResult = await summaryResponse.json();
            console.log('Summary calculation result:', summaryResult);

            if (!summaryResult.success && summaryResult.totalEmployees === 0) {
                showAlert('Failed to calculate attendance summary. Please ensure attendance data exists.', 'error');
                return;
            }

            console.log('✓ Attendance summary calculated successfully');
            console.log('Step 2: Generating payroll...');

            // STEP 2: Generate payroll using the calculated summaries
            const response = await fetchWithAuth(`/payroll/generate?year=${year}&month=${monthNum}`, {
                method: 'POST'
            });

            const result = await response.json();
            console.log('Payroll generation result:', result);

            if (result.success) {
                const message = `Payroll generated successfully!\n` +
                    `Generated for ${result.generatedPayrolls?.length || 0} employees`;

                if (result.errors && result.errors.length > 0) {
                    console.warn('Some errors occurred:', result.errors);
                }

                showAlert(message, 'success');
                closeGeneratePayrollModal();

                // Reload payroll data
                const payrollMonthElement = document.getElementById('payrollMonth');
                if (payrollMonthElement) {
                    payrollMonthElement.value = month;
                }
                loadPayrollData();

            } else {
                showAlert(result.message || 'Failed to generate payroll', 'error');
            }
        } catch (error) {
            console.error('Error generating payroll:', error);
            showAlert('Error generating payroll: ' + error.message, 'error');
        }
    });
}

// View payroll details
async function viewPayrollDetails(employeeId, payrollMonth) {
    try {
        const [year, month] = payrollMonth.split('-');

        const response = await fetchWithAuth(`/payroll/employee?employeeId=${employeeId}&year=${year}&month=${month}`);
        const result = await response.json();

        if (result.success) {
            displayPayrollDetails(result);
        } else {
            showAlert(result.message || 'Failed to load payroll details', 'error');
        }
    } catch (error) {
        console.error('Error loading payroll details:', error);
        showAlert('Error loading payroll details: ' + error.message, 'error');
    }
}

// Display payroll details in modal
function displayPayrollDetails(result) {
    const payroll = result.payroll;
    const content = document.getElementById('payrollDetailsContent');

    if (!content) {
        console.error('Payroll details content element not found');
        return;
    }

    content.innerHTML = `
        <div class="payroll-details-section">
            <h4>Employee Information</h4>
            <div class="payroll-details-grid">
                <div class="payroll-detail-item">
                    <span class="payroll-detail-label">Employee ID:</span>
                    <span class="payroll-detail-value">${payroll.employeeId}</span>
                </div>
                <div class="payroll-detail-item">
                    <span class="payroll-detail-label">Employee Name:</span>
                    <span class="payroll-detail-value">${result.employeeName}</span>
                </div>
                <div class="payroll-detail-item">
                    <span class="payroll-detail-label">Payroll Month:</span>
                    <span class="payroll-detail-value">${formatMonthYear(payroll.payrollMonth)}</span>
                </div>
                <div class="payroll-detail-item">
                    <span class="payroll-detail-label">Status:</span>
                    <span class="payroll-detail-value status-badge status-badge-${payroll.status.toLowerCase()}">${payroll.status}</span>
                </div>
            </div>
        </div>
        
        <div class="payroll-details-section">
            <h4>Attendance Summary</h4>
            <div class="payroll-details-grid">
                <div class="payroll-detail-item">
                    <span class="payroll-detail-label">Actual Work Hours:</span>
                    <span class="payroll-detail-value">${formatNumber(payroll.actualWorkHours)} hours</span>
                </div>
                <div class="payroll-detail-item">
                    <span class="payroll-detail-label">Max Work Hours:</span>
                    <span class="payroll-detail-value">${formatNumber(payroll.maxWorkHours)} hours</span>
                </div>
                <div class="payroll-detail-item">
                    <span class="payroll-detail-label">Overtime Hours:</span>
                    <span class="payroll-detail-value">${formatNumber(payroll.otHours)} hours</span>
                </div>
                <div class="payroll-detail-item">
                    <span class="payroll-detail-label">Attendance Rate:</span>
                    <span class="payroll-detail-value">${formatNumber(payroll.attendanceRate)}%</span>
                </div>
            </div>
        </div>
        
        <div class="payroll-details-section">
            <h4>Salary Calculation</h4>
            <div class="payroll-breakdown">
                <div class="payroll-breakdown-item">
                    <span>Basic Salary:</span>
                    <span>$${formatCurrency(payroll.basicSalary)}</span>
                </div>
                <div class="payroll-breakdown-item">
                    <span>Regular Hours Pay (${Math.min(payroll.actualWorkHours, payroll.maxWorkHours)} hours):</span>
                    <span>$${formatCurrency(payroll.grossSalary - payroll.otAmount)}</span>
                </div>
                <div class="payroll-breakdown-item">
                    <span>Overtime Pay (${payroll.otHours} hours × ${payroll.otRate}x):</span>
                    <span>$${formatCurrency(payroll.otAmount)}</span>
                </div>
                <div class="payroll-breakdown-item">
                    <span>Gross Salary:</span>
                    <span class="payroll-detail-value amount">$${formatCurrency(payroll.grossSalary)}</span>
                </div>
                <div class="payroll-breakdown-item">
                    <span>Deductions (Attendance ${formatNumber(payroll.attendanceRate)}%):</span>
                    <span class="payroll-detail-value deduction">-$${formatCurrency(payroll.deductions)}</span>
                </div>
                <div class="payroll-breakdown-item">
                    <span><strong>Net Salary:</strong></span>
                    <span><strong>$${formatCurrency(payroll.netSalary)}</strong></span>
                </div>
            </div>
        </div>
        
        <div class="payroll-details-section">
            <h4>Payroll Parameters</h4>
            <div class="payroll-details-grid">
                <div class="payroll-detail-item">
                    <span class="payroll-detail-label">OT Rate Multiplier:</span>
                    <span class="payroll-detail-value">${payroll.otRate}x</span>
                </div>
                <div class="payroll-detail-item">
                    <span class="payroll-detail-label">Calculation Date:</span>
                    <span class="payroll-detail-value">${new Date(payroll.updatedAt).toLocaleDateString()}</span>
                </div>
            </div>
        </div>
    `;

    const modal = document.getElementById('payrollDetailsModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

// Close payroll details modal
function closePayrollDetailsModal() {
    const modal = document.getElementById('payrollDetailsModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// Edit OT rate
function editOtRate(employeeId, payrollMonth, currentRate) {
    document.getElementById('editOtEmployeeId').value = employeeId;
    document.getElementById('editOtPayrollMonth').value = payrollMonth;
    document.getElementById('editOtRate').value = currentRate;

    // Find employee name
    const employee = currentPayrollData.find(item => item.payroll.employeeId === employeeId);
    const employeeName = employee ? employee.employeeName : 'Unknown Employee';

    const detailsElement = document.getElementById('editOtEmployeeDetails');
    if (detailsElement) {
        detailsElement.innerHTML = `
            <strong>Employee:</strong> ${employeeName} (${employeeId})<br>
            <strong>Payroll Month:</strong> ${formatMonthYear(payrollMonth)}<br>
            <strong>Current OT Rate:</strong> ${currentRate}x
        `;
    }

    const modal = document.getElementById('editOtRateModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

// Close edit OT rate modal
function closeEditOtRateModal() {
    const modal = document.getElementById('editOtRateModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// Update OT rate
const editOtFormElement = document.getElementById('editOtRateForm');
if (editOtFormElement) {
    editOtFormElement.addEventListener('submit', async function(e) {
        e.preventDefault();

        const employeeId = document.getElementById('editOtEmployeeId').value;
        const payrollMonth = document.getElementById('editOtPayrollMonth').value;
        const otRate = document.getElementById('editOtRate').value;

        try {
            const response = await fetchWithAuth(`/payroll/ot-rate?employeeId=${employeeId}&payrollMonth=${payrollMonth}&otRate=${otRate}`, {
                method: 'PUT'
            });

            const result = await response.json();

            if (result.success) {
                showAlert('OT rate updated successfully!', 'success');
                closeEditOtRateModal();

                // Reload payroll data
                loadPayrollData();

            } else {
                showAlert(result.message || 'Failed to update OT rate', 'error');
            }
        } catch (error) {
            console.error('Error updating OT rate:', error);
            showAlert('Error updating OT rate: ' + error.message, 'error');
        }
    });
}

// Mark as paid
async function markAsPaid(employeeId, payrollMonth) {
    if (!confirm(`Mark payroll for ${employeeId} as paid? This action cannot be undone.`)) {
        return;
    }

    try {
        const response = await fetchWithAuth(`/payroll/mark-paid?employeeId=${employeeId}&payrollMonth=${payrollMonth}`, {
            method: 'PUT'
        });

        const result = await response.json();

        if (result.success) {
            showAlert('Payroll marked as paid!', 'success');
            loadPayrollData();
        } else {
            showAlert(result.message || 'Failed to mark as paid', 'error');
        }
    } catch (error) {
        console.error('Error marking as paid:', error);
        showAlert('Error marking as paid: ' + error.message, 'error');
    }
}

// Export payroll report
async function exportPayrollReport() {
    if (currentPayrollData.length === 0) {
        showAlert('No payroll data to export', 'error');
        return;
    }

    try {
        const [year, month] = currentPayrollMonth.split('-');

        const response = await fetchWithAuth(`/payroll/monthly-report?year=${year}&month=${month}`);
        const result = await response.json();

        if (result.success) {
            exportToExcel(result);
        } else {
            showAlert('Failed to export payroll report', 'error');
        }
    } catch (error) {
        console.error('Error exporting payroll:', error);
        showAlert('Error exporting payroll: ' + error.message, 'error');
    }
}

// Export to Excel format
function exportToExcel(result) {
    // Create CSV content
    let csvContent = "Payroll Report - " + formatMonthYear(currentPayrollMonth) + "\n\n";

    // Add summary
    csvContent += "SUMMARY\n";
    csvContent += `Total Employees,${result.employeeCount}\n`;
    csvContent += `Total Gross Salary,$${formatCurrency(result.totalGrossSalary)}\n`;
    csvContent += `Total Deductions,$${formatCurrency(result.totalDeductions)}\n`;
    csvContent += `Total Net Salary,$${formatCurrency(result.totalNetSalary)}\n\n`;

    // Add details
    csvContent += "DETAILS\n";
    csvContent += "Employee ID,Employee Name,Basic Salary,Work Hours,OT Hours,OT Amount,Gross Salary,Deductions,Net Salary,Attendance Rate,Status\n";

    result.payrolls.forEach(item => {
        const p = item.payroll;
        csvContent += `${p.employeeId},"${item.employeeName}",${p.basicSalary},${p.actualWorkHours},${p.otHours},${p.otAmount},${p.grossSalary},${p.deductions},${p.netSalary},${p.attendanceRate},${p.status}\n`;
    });

    // Create and download file
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);

    link.setAttribute('href', url);
    link.setAttribute('download', `payroll-report-${currentPayrollMonth}.csv`);
    link.style.visibility = 'hidden';

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    showAlert('Payroll report exported successfully!', 'success');
}

// Utility functions
function formatCurrency(amount) {
    return parseFloat(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
}

function formatNumber(num) {
    return parseFloat(num).toFixed(2);
}

function formatMonthYear(monthYear) {
    const [year, month] = monthYear.split('-');
    const date = new Date(year, month - 1);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long' });
}

function showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = '<div class="loading">Loading...</div>';
    }
}

function showNoDataMessage() {
    const noDataElement = document.getElementById('noPayrollData');
    const summaryElement = document.getElementById('payrollSummary');
    const tableElement = document.querySelector('.table-container');

    if (noDataElement) noDataElement.style.display = 'block';
    if (summaryElement) summaryElement.style.display = 'none';
    if (tableElement) tableElement.style.display = 'none';
}

// Make functions globally accessible
window.initPayroll = initPayroll;
window.loadPayrollData = loadPayrollData;
window.filterPayrollTable = filterPayrollTable;
window.showGeneratePayrollModal = showGeneratePayrollModal;
window.closeGeneratePayrollModal = closeGeneratePayrollModal;
window.viewPayrollDetails = viewPayrollDetails;
window.closePayrollDetailsModal = closePayrollDetailsModal;
window.editOtRate = editOtRate;
window.closeEditOtRateModal = closeEditOtRateModal;
window.markAsPaid = markAsPaid;
window.exportPayrollReport = exportPayrollReport;