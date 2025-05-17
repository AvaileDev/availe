function formatTable(headers, rows, rowClassFunc) {
    let ths = headers.map(h => `<th>${h}</th>`).join("");
    let trs = rows.map(row => {
        let cls = rowClassFunc ? ` class="${rowClassFunc(row)}"` : "";
        let tds = row.map(cell => `<td>${cell}</td>`).join("");
        return `<tr${cls}>${tds}</tr>`;
    }).join("");
    return `<table><tr>${ths}</tr>${trs}</table>`;
}

function renderDashboard(data) {
    let ridersTable = formatTable(
        ["id", "Name"],
        data.riders.map(r => [r.id, r.name])
    );
    let driversTable = formatTable(
        ["id", "Name", "Plate"],
        data.drivers.map(d => [d.id, d.name, d.car_plate])
    );
    let trips = data.trips.slice().reverse();
    let tripsTable = formatTable(
        ["id", "Rider", "Driver", "From", "To", "Remaining km", "Status"],
        trips.map(t => [
            t.id,
            t.rider_id,
            t.driver_id || "-",
            t.origin,
            t.dest,
            t.remaining_km.toFixed(1),
            t.status
        ]),
        row => {
            const status = row[6];
            if (status === "requested") return "requested";
            if (status === "accepted") return "accepted";
            if (status === "started") return "started";
            if (status === "ended") return "ended";
            return "";
        }
    );
    document.getElementById("dashboard-content").innerHTML = `
    <h2>Riders (${data.riders.length})</h2>
    ${ridersTable}
    <h2>Drivers (${data.drivers.length})</h2>
    ${driversTable}
    <h2>Trips (${trips.length})</h2>
    ${tripsTable}
  `;
}

async function fetchAndUpdate() {
    try {
        let resp = await fetch("/dashboard.json");
        let data = await resp.json();
        renderDashboard(data);
    } catch (e) {
        document.getElementById("dashboard-content").innerHTML =
            "<div style='color:red;'>Failed to fetch dashboard data</div>";
    }
}

fetchAndUpdate().catch(console.error);
setInterval(() => {
    fetchAndUpdate().catch(console.error);
}, 1000);
