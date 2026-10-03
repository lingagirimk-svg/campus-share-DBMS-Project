async function api(url, options={}) {
  const r = await fetch(url, options);
  const data = await r.json();
  if (!r.ok || data.error) throw new Error(data.error || "Request failed");
  return data;
}
const esc = v => String(v ?? "").replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;");

async function loadDashboard(){
  const d=await api("/api/dashboard");
  const items=[
    ["Total Resources",d.totalResources],["Available",d.availableResources],
    ["Borrowed",d.borrowedResources],["Maintenance",d.maintenanceResources],
    ["Bookings",d.totalBookings],["Overdue",d.overdueItems]
  ];
  document.getElementById("cards").innerHTML=items.map(x=>`<div class="card"><span>${x[0]}</span><b>${x[1]}</b></div>`).join("");
}
async function loadResources(){
  const q=document.getElementById("search").value;
  const rows=await api("/api/resources?search="+encodeURIComponent(q));
  document.getElementById("resourceRows").innerHTML=rows.map(r=>`<tr>
    <td>${r.resource_id}</td><td>${esc(r.resource_name)}</td><td>${esc(r.category)}</td>
    <td>${esc(r.location)}</td><td>${esc(r.condition_status)}</td><td class="status">${esc(r.availability_status)}</td><td>${r.booking_count}</td>
  </tr>`).join("");
}
async function loadBookings(){
  const rows=await api("/api/bookings");
  document.getElementById("bookingRows").innerHTML=rows.map(r=>`<tr>
    <td>${r.booking_id}</td><td>${esc(r.student)}</td><td>${esc(r.resource_name)}</td>
    <td>${r.booking_date}</td><td>${r.start_time} - ${r.end_time}</td><td>${r.booking_status}</td>
  </tr>`).join("");
}
async function loadBorrow(){
  const rows=await api("/api/borrow");

  document.getElementById("borrowRows").innerHTML=rows.map(r=>`<tr>
    <td>${r.transaction_id}</td>
    <td>${esc(r.student)}</td>
    <td>${esc(r.resource_name)}</td>
    <td>${r.issued_date}</td>
    <td>${r.due_date}</td>
    <td>${r.returned_date ?? "Not returned"}</td>
    <td>
      ${r.returned_date
        ? "Completed"
        : `<button onclick="returnResource(${r.transaction_id})">Return</button>`}
    </td>
  </tr>`).join("");
}
async function returnResource(transactionId){
  try{
    const d=await api(`/api/return/${transactionId}`,{
      method:"POST",
      headers:{"Content-Type":"application/json"},
      body:JSON.stringify({condition:"GOOD"})
    });

    alert(d.message);
    loadAll();
  }catch(err){
    alert(err.message);
  }
}
async function loadMaintenance(){
  const rows=await api("/api/maintenance");
  document.getElementById("maintenanceRows").innerHTML=rows.map(r=>`<tr>
    <td>${esc(r.resource_name)}</td><td>${esc(r.problem)}</td><td>${r.reported_date}</td><td>${r.repair_status}</td>
  </tr>`).join("");
}
async function loadAll(){try{await Promise.all([loadDashboard(),loadResources(),loadBookings(),loadBorrow(),loadMaintenance()]);}catch(e){alert(e.message)}}

document.getElementById("bookingForm").addEventListener("submit",async e=>{
  e.preventDefault(); const f=new FormData(e.target); const body=Object.fromEntries(f);
  try{const d=await api("/api/bookings",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(body)});
    document.getElementById("bookingMsg").textContent=d.message; e.target.reset(); loadAll();
  }catch(err){document.getElementById("bookingMsg").textContent=err.message}
});
document.getElementById("borrowForm").addEventListener("submit",async e=>{
  e.preventDefault(); const f=new FormData(e.target); const body=Object.fromEntries(f);
  try{const d=await api("/api/borrow",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(body)});
    document.getElementById("borrowMsg").textContent=d.message; e.target.reset(); loadAll();
  }catch(err){document.getElementById("borrowMsg").textContent=err.message}
});
loadAll();
