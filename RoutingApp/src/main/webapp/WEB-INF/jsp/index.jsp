<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
	<head>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
		<link rel="stylesheet" href="https://unpkg.com/leaflet@1.0.3/dist/leaflet.css"
		   integrity="sha512-07I2e+7D8p6he1SIM+1twR5TIrhUQn9+I6yjqD53JQjFiMf8EtC93ty0/5vJTZGF8aAocvHYNEDJajGdNx1IsQ=="
		   crossorigin=""/>
	   <script src="https://unpkg.com/leaflet@1.0.3/dist/leaflet.js"
		   integrity="sha512-A7vV8IFfih/D732iSSKi20u/ooOfj/AGehOKq0f4vLT1Zr2Y+RX7C+w8A1gaSasGtRUZpF/NZgzSAu4/Gc41Lg=="
		   crossorigin=""></script>
		<script src="${pageContext.request.contextPath}/js/leaflet-color-markers.js"></script>
	</head>
    <body>
        <h1>Urban Evacuation Central Management</h1>

		<div style="width:100%">
			<table style="width:100%; height:600px;">
				<tr>
					<td style="width:70%">
	       				<div id="map" style="width:100%;height:100%;"></div>
					</td>
					<td style="width:30%">
						<table id="usersRandom">
							<thead>
								<th>UID</th>
							</thead>
							
							<tbody>
							
							</tbody>
						</table>
						<table id="usersSame">
							<thead>
								<th>UID</th>
							</thead>
							
							<tbody>
							
							</tbody>
						</table>
					</td>
				</tr>
			</table>
		</div>
		
 		<h3>Road Management</h3>
		
		<p>
			<form name="roads">
				Node Id: <input type="number" name="id"/>
				<input type="button" onclick="openRoad(this.form.id.value)" value="Open Road"/>
				<input type="button" onclick="closeRoad(this.form.id.value)" value="Close Road"/>
				<input type="reset" value="Cancel"/>
			</form>
		</p>
<!-- 		<p>Open all Closed Roads: <input type="button" onclick="openAllRoads()" value="Open All"/></p> -->

		<h3>Simulate Users</h3>
		<p>Generate 1000 Random Users: <input type="button" onclick="generateRandomRoutes(1000)" value="Generate"/></p>
		<p>Generate 1000 Random Users at Colesuem: <input type="button" onclick="generateRoutes(1000)" value="Generate"/></p>

        <h3>Add points</h3>

        <form name="login">
            Lat: <input type="number" name="newLat"/>
            Long: <input type="number" name="newLong"/>
            <input type="button" onclick="submitPoint(this.form)" value="Add Point"/>
            <input type="reset" value="Cancel"/>
        </form>
        <p>Sample points:</p>
        <p>University Town Center: <input type="button" onclick="dropPoint(39.6446268,-80.0003815)" value="Drop"/></p>
        <p>Health Sciences Campus: <input type="button" onclick="dropPoint(39.6562542,-79.9594877)" value="Drop"/></p>
        <p>Morgantown Airport: <input type="button" onclick="dropPoint(39.6441022,-79.9200768)" value="Drop"/></p>
		<p>Clear All points: <input type="button" onclick="clearPoints()" value="Clear"/></p>
		
		<script language="javascript">
			var contextPath = "${pageContext.request.contextPath}";
		
			var greenIcon = new L.Icon({
				iconUrl: 'https://cdn.rawgit.com/pointhi/leaflet-color-markers/master/img/marker-icon-green.png',
				shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
				iconSize: [25, 41],
				iconAnchor: [12, 41],
				popupAnchor: [1, -34],
				shadowSize: [41, 41]
			});
			
			var orangeIcon = new L.Icon({
				iconUrl: 'https://cdn.rawgit.com/pointhi/leaflet-color-markers/master/img/marker-icon-orange.png',
				shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
				iconSize: [25, 41],
				iconAnchor: [12, 41],
				popupAnchor: [1, -34],
				shadowSize: [41, 41]
			});
			
			var dotIcon = new L.Icon({
				iconUrl: "https://cdn4.iconfinder.com/data/icons/abstract-symbols/100/abstract_symbol-09-128.png",
				iconSize: [10, 10],
				iconAnchor: [5, 5]
			});
			
			var randomUIDs = [
				"a1cd57fe-6781-0d2c-5552-b5f59b3d2d17",
				"a3f9723d-84b7-0991-15fb-d70b9e17cc65",
				"5c459cbd-f7e3-edeb-5551-453bf87b5e5d",
				"775aca48-01a2-4df5-a964-b2c240dd15e9",
				"0a6d4386-866f-4e1c-058d-c13fc1e958a3"
			];
			
			var sameUIDs = [
				
			];
			
			var map;
			var ajaxRequest;
			var plotlist;
			var plotlayers=[];
			var markers = [];
		
			map = new L.Map('map');

			// create the tile layer with correct attribution
			var osmUrl='http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
			var osmAttrib='Map data © <a href="http://openstreetmap.org">OpenStreetMap</a> contributors';
			var osm = new L.TileLayer(osmUrl, {minZoom: 8, maxZoom: 20, attribution: osmAttrib});		

			// start the map in South-East England
			map.setView(new L.LatLng(39.6361264, -79.9559962), 13);
			map.addLayer(osm);
			
			L.marker([39.669534, -79.862972], {icon:greenIcon}).addTo(map);
			L.marker([39.573246, -79.976842], {icon:greenIcon}).addTo(map);
			L.marker([39.666485, -80.020825], {icon:greenIcon}).addTo(map);
			
			function submitPoint(form){
               dropPoint(form.newLat.value, form.newLong.value)
            }
			
			function dropStartPoint(lat, lon) {
				markers.push(L.marker([lat, lon], {icon:orangeIcon}).bindPopup("<b>Start Node</b>").addTo(map));
			}
			
			function dropPoint(lat, lon, id) {
				markers.push(L.marker([lat, lon], {icon:dotIcon}).bindPopup("<b>" + id + "</b>").addTo(map));
			}
			
			function clearPoints() {
				markers.forEach(function(marker) {
					map.removeLayer(marker);
				});
			}
			
 			$(function() {
 				randomUIDs.forEach(function(uid) {
					var tblRow = "<tr>" + "<td onclick='displayRoute(\"" + uid + "\")'>" + uid + "</td></tr>";
					$(tblRow).appendTo("#usersRandom tbody");
				});
 				
 				sameUIDs.forEach(function(uid) {
					var tblRow = "<tr>" + "<td onclick='displayRoute(\"" + uid + "\")'>" + uid + "</td></tr>";
					$(tblRow).appendTo("#usersSame tbody");
				});
			});
			
			function generateRandomRoutes(num) {
				for (var i = 0; i < num; i++) {
					var id = guid();
					var lat = 39.572508 + Math.random() * (39.6673743 - 39.572508);
					var lon = -79.9190856 + Math.random() * (-80.0354549 - -79.9190856);
					
					$.getJSON(contextPath + "/api/users/add/" + id + "/" + lat + "/" + lon + "/", function(response) {
					});
					
					$.getJSON(contextPath + "/route/" + id + "/", function(response) {
					});
				}
			}
			
			function generateRoutes(num) {
				for (var i = 0; i < num; i++) {
					var id = guid();
					var lat = 39.6494907;
					var lon = -79.9826654;
					
					$.getJSON(contextPath + "/api/users/add/" + id + "/" + lat + "/" + lon + "/", function(response) {
					});
					
					$.getJSON(contextPath + "/route/" + id + "/", function(response) {
					});
				}
			}
			
			function guid() {
				function s4() {
					return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
				}
		
				return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
			}
			
			function displayRoute(uid) {
				clearPoints();
				
				// Start Node
				$.getJSON(contextPath + "/api/users/" + uid + "/", function(response) {
					dropStartPoint(response.lat, response.lon, "Start Node");
				});
				
				// Route Points
				$.getJSON(contextPath + "/route/" + uid + "/", function(response) {
					response.route.forEach(function(node) {
						dropPoint(node.latitude, node.longitude, node.id);
					});
				});
			}
			
			function openRoad(id) {
				$.getJSON(contextPath + "/route/open/" + id + "/", function(response) {
					if (response) {
						alert("Road closed.");
					} else {
						alert("Road opened.");
					}
				});
			}
			
			function closeRoad(id) {
				$.getJSON(contextPath + "/route/close/" + id + "/", function(response) {
					if (response) {
						alert("Road closed.");
					} else {
						alert("Road opened.");
					}
				});
			}
			
			function openAllRoads() {
				$.getJSON(contextPath + "/api/closed/", function(reponse) {
					reponse.forEach(function(closedRoad) {
						openRoad(closedRoad.id);
					});
				});
			}
		</script>
    </body>
</html>

