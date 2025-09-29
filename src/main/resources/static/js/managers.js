 let departments = [], employees = [], systemUsers = [], departmentManagers = [];

    function showTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    document.getElementById(tabName).classList.add('active');
    event.target.classList.add('active');
    if (tabName === 'departmentManagers') loadDepartmentManagers();
    else if (tabName === 'systemUsers') loadSystemUsers();
}

    async function fetchWithAuth(url, options = {}) {
    console.log('Making API request to:', url, 'with options:', options);
    const response = await fetch(url, {...options, credentials:'include'});
    console.log('API response status:', response.status);

    if (response.status === 401) {
    alert('Session expired. Please login again.');
    window.location.href = '/systemUserLogin';
    throw new Error('Unauthorized');
}
    return response;
}

    function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.innerHTML = `<span>${message}</span>
        <button onclick="this.parentElement.remove()" style="float:right; background:none; border:none; cursor:pointer;">×</button>`;
    document.getElementById('alertContainer').appendChild(alertDiv);
    setTimeout(() => alertDiv.remove(), 5000);
}

    async function loadDepartments() {
    const res = await fetchWithAuth('/api/departments');
    if (!res.ok) throw new Error('Failed to load departments');
    departments = await res.json();
}

    async function loadEmployees() {
    const res = await fetchWithAuth('/api/employees');
    if (!res.ok) throw new Error('Failed to load employees');
    employees = await res.json();
}

    async function loadDepartmentManagers() {
    const loadingEl = document.getElementById('loadingDeptManagers');
    const tbody = document.querySelector('#departmentManagersTable tbody');
    loadingEl.style.display = 'block'; tbody.innerHTML = '';
    try {
    await Promise.all([loadDepartments(), loadEmployees()]);
    departmentManagers = departments.map(dept => {
    const managerEmployee = employees.find(emp => emp.id === dept.managerId);
    return {
    departmentId: dept.id,
    departmentName: dept.departmentName,
    managerId: dept.managerId,
    managerName: managerEmployee ? managerEmployee.fullName : 'Not Assigned',
    managerEmail: managerEmployee ? managerEmployee.email : 'N/A',
    employeeCount: dept.employeeCount || 0
};
});
    refreshDepartmentManagersTable();
    populateAssignFormDropdowns();
} catch (err) {
    console.error('Error loading department managers:', err);
    showAlert('Error loading department managers: ' + err.message, 'error');
} finally { loadingEl.style.display = 'none'; }
}

    function refreshDepartmentManagersTable() {
    const tbody = document.querySelector('#departmentManagersTable tbody');
    tbody.innerHTML = '';
    if (departmentManagers.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No department managers found</td></tr>';
    return;
}
    departmentManagers.forEach(mgr => {
    const row = document.createElement('tr');
    row.innerHTML = `
          <td>${mgr.departmentId}</td>
          <td>${mgr.departmentName}</td>
          <td>${mgr.managerName}</td>
          <td>${mgr.managerId || 'N/A'}</td>
          <td>${mgr.managerEmail}</td>
          <td>${mgr.employeeCount}</td>
          <td>
            <button class="btn btn-warning btn-sm" onclick="editDepartmentManager('${mgr.departmentId}')"><i class="fas fa-edit"></i> Change</button>
            ${mgr.managerId ? `<button class="btn btn-secondary btn-sm" onclick="removeManager('${mgr.departmentId}')"><i class="fas fa-times"></i> Remove</button>` : ''}
          </td>`;
    tbody.appendChild(row);
});
}

    function populateAssignFormDropdowns() {
    const deptSelect = document.getElementById('assignDepartment');
    const empSelect = document.getElementById('assignEmployee');
    deptSelect.innerHTML = '<option value="">Select Department</option>';
    empSelect.innerHTML = '<option value="">Select Employee</option>';
    departments.filter(d => !d.managerId).forEach(dept => {
    const opt = document.createElement('option');
    opt.value = dept.id; opt.textContent = `${dept.departmentName} (${dept.id})`;
    deptSelect.appendChild(opt);
});
    employees.forEach(emp => {
    const opt = document.createElement('option');
    opt.value = emp.id; opt.textContent = `${emp.fullName} (${emp.id}) - ${emp.department ? emp.department.departmentName : 'No Department'}`;
    empSelect.appendChild(opt);
});
}

    async function editDepartmentManager(deptId) {
    const dept = departments.find(d => d.id === deptId); if (!dept) return;
    document.getElementById('editDepartmentId').value = dept.id;
    document.getElementById('editDepartmentName').value = `${dept.departmentName} (${dept.id})`;
    const empSelect = document.getElementById('editManagerEmployee');
    empSelect.innerHTML = '<option value="">Select Employee</option>';
    employees.forEach(emp => {
    const opt = document.createElement('option');
    opt.value = emp.id; opt.textContent = `${emp.fullName} (${emp.id})`;
    if (emp.id === dept.managerId) opt.selected = true;
    empSelect.appendChild(opt);
});
    document.getElementById('editManagerModal').style.display = 'block';
}

    function closeEditManagerModal(){document.getElementById('editManagerModal').style.display='none';}

    async function removeManager(deptId){
    if(!confirm('Are you sure you want to remove the manager?')) return;
    try{
    const res=await fetchWithAuth(`/api/managers/department/${deptId}/manager`,{method:'DELETE'});
    const result=await res.json();
    if(res.ok){showAlert('Manager removed successfully!','success'); await loadDepartmentManagers();}
    else showAlert(result.message||'Failed to remove manager','error');
}catch(err){console.error(err); showAlert('Error removing manager: '+err.message,'error');}
}

    async function loadSystemUsers(){
    const loadingEl=document.getElementById('loadingSystemUsers');
    const tbody=document.querySelector('#systemUsersTable tbody');
    loadingEl.style.display='block'; tbody.innerHTML='';
    try{
    const res=await fetchWithAuth('/api/managers/system-users');
    if(res.ok) {
    systemUsers=await res.json();
    console.log('Loaded system users:', systemUsers);
} else {
    console.error('Failed to load system users, status:', res.status);
    systemUsers=[];
}
    refreshSystemUsersTable();
}catch(err){
    console.error('Error loading system users:',err);
    systemUsers=[];
    refreshSystemUsersTable();
    showAlert('Error loading system users: ' + err.message, 'error');
}
    finally{loadingEl.style.display='none';}
}

    function refreshSystemUsersTable(){
    const tbody=document.querySelector('#systemUsersTable tbody');
    const roleFilter=document.getElementById('roleFilter').value;
    tbody.innerHTML='';
    let filtered=systemUsers;
    if(roleFilter) filtered=systemUsers.filter(u=>u.role===roleFilter);
    if(filtered.length===0){
    tbody.innerHTML='<tr><td colspan="6" style="text-align:center;">No system users found</td></tr>';
    return;
}
    filtered.forEach(u=>{
    // Format role display
    const roleDisplay = u.role.replace('-', ' ').toUpperCase();
    const roleBadgeClass = u.role === 'hr-manager' ? 'badge-success' : 'badge-warning';

    const row=document.createElement('tr');
    row.innerHTML=`
          <td>${u.userName}</td>
          <td><span class="badge ${roleBadgeClass}">${roleDisplay}</span></td>
          <td>${u.email||'Not provided'}</td>
          <td>${u.phoneNumber||'Not provided'}</td>
          <td><span class="badge badge-success">Active</span></td>
          <td>
            <button class="btn btn-secondary btn-sm" onclick="viewUserDetails('${u.userName}','${u.role}')">
              <i class="fas fa-eye"></i> View Details
            </button>
          </td>`;
    tbody.appendChild(row);
});
}

    function filterSystemUsers(){refreshSystemUsersTable();}

    function viewUserDetails(username,role){
    const user=systemUsers.find(u=>u.userName===username&&u.role===role);
    if(user) {
    // Create a more detailed modal for user details
    const modal = document.createElement('div');
    modal.style.cssText = 'position:fixed; z-index:1000; left:0; top:0; width:100%; height:100%; background-color:rgba(0,0,0,0.5);';

    const modalContent = document.createElement('div');
    modalContent.style.cssText = 'background:#fff; margin:5% auto; padding:20px; border-radius:10px; width:80%; max-width:500px;';

    modalContent.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
          <h3><i class="fas fa-user"></i> System User Details</h3>
          <span onclick="document.body.removeChild(this.closest('.user-details-modal'))"
                style="color:#aaa; font-size:28px; font-weight:bold; cursor:pointer;">&times;</span>
        </div>
        <div style="line-height:1.6;">
          <p><strong>Username:</strong> ${user.userName}</p>
          <p><strong>Role:</strong> <span class="badge ${user.role==='hr-manager'?'badge-success':'badge-warning'}">${user.role}</span></p>
          <p><strong>Email:</strong> ${user.email || 'Not provided'}</p>
          <p><strong>Phone Number:</strong> ${user.phoneNumber || 'Not provided'}</p>
          <p><strong>Status:</strong> <span class="badge badge-success">Active</span></p>
          <p><strong>Account Type:</strong> System User</p>
        </div>
        <div style="text-align:right; margin-top:20px;">
          <button onclick="document.body.removeChild(this.closest('.user-details-modal'))"
                  class="btn btn-secondary"><i class="fas fa-times"></i> Close</button>
        </div>
      `;

    modal.className = 'user-details-modal';
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    // Close modal when clicking outside
    modal.onclick = function(e) {
    if (e.target === modal) {
    document.body.removeChild(modal);
}
};
} else {
    showAlert('User details not found', 'error');
}
}

    // SINGLE EVENT LISTENER FOR ASSIGN MANAGER FORM
    document.addEventListener('DOMContentLoaded', function() {
    console.log('Manage Managers initialized');
    loadDepartmentManagers();

    // Assign manager form handler
    document.getElementById('assignManagerForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    console.log('Assign manager form submitted');

    const formData = new FormData(e.target);
    const departmentId = formData.get('departmentId');
    const employeeId = formData.get('employeeId');

    console.log('Assigning manager - Department:', departmentId, 'Employee:', employeeId);

    if (!departmentId || !employeeId) {
    showAlert('Please select both department and employee', 'error');
    return;
}

    try {
    const response = await fetchWithAuth(`/api/managers/department/${departmentId}/manager`, {
    method: 'PUT',
    headers: {
    'Content-Type': 'application/json',
},
    body: JSON.stringify({ managerId: employeeId })
});

    const result = await response.json();
    console.log('API response:', result);

    if (response.ok) {
    showAlert('Manager assigned successfully!', 'success');
    e.target.reset();
    await loadDepartmentManagers();
} else {
    showAlert(result.message || 'Failed to assign manager', 'error');
}
} catch (error) {
    console.error('Error assigning manager:', error);
    showAlert('Error assigning manager: ' + error.message, 'error');
}
});

    // Edit manager form handler
    document.getElementById('editManagerForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    console.log('Edit manager form submitted');

    const departmentId = document.getElementById('editDepartmentId').value;
    const employeeId = document.getElementById('editManagerEmployee').value;

    try {
    const response = await fetchWithAuth(`/api/managers/department/${departmentId}/manager`, {
    method: 'PUT',
    headers: {
    'Content-Type': 'application/json',
},
    body: JSON.stringify({ managerId: employeeId })
});

    const result = await response.json();

    if (response.ok) {
    showAlert('Manager updated successfully!', 'success');
    closeEditManagerModal();
    await loadDepartmentManagers();
} else {
    showAlert(result.message || 'Failed to update manager', 'error');
}
} catch (error) {
    console.error('Error updating manager:', error);
    showAlert('Error updating manager: ' + error.message, 'error');
}
});
});

    window.onclick=function(e){const modal=document.getElementById('editManagerModal'); if(e.target===modal) closeEditManagerModal();}