	window.addEventListener("load", function () {

		var chart = new CanvasJS.Chart("chartContainer", {
			theme: "theme3",
						animationEnabled: true,
			title:{
				text: "Llamadas"
			},
			axisY: {
				title: "Entrantes"
			},
			axisY2: {
				title: "Salientes"
			},
			data: [
			{
				type: "column",
				name: "Entrantes",
				legendText: "Entrantes",
				showInLegend: true,
				dataPoints:[
				{label: "Domingo", y: interfaz.dia(0,0)},
				{label: "Lunes", y: interfaz.dia(1,0)},
				{label: "Martes", y: interfaz.dia(2,0)},
				{label: "Miercoles", y: interfaz.dia(3,0)},
				{label: "Jueves", y: interfaz.dia(4,0)},
				{label: "Viernes", y: interfaz.dia(5,0)},
				{label: "Sabado", y: interfaz.dia(6,0)}

				]
			},
			{
				type: "column",
				name: "Salientes",
				legendText: "Salientes",
				axisYType: "secondary",
				showInLegend: true,
				dataPoints:[
				{label: "Domingo", y: interfaz.dia(0,1)},
				{label: "Lunes", y: interfaz.dia(1,1)},
				{label: "Martes", y: interfaz.dia(2,1)},
				{label: "Miercoles", y: interfaz.dia(3,1)},
				{label: "Jueves", y: interfaz.dia(4,1)},
				{label: "Viernes", y: interfaz.dia(5,1)},
				{label: "Sabado", y: interfaz.dia(6,1)}

				]
			}

			],
			legend:{
				cursor:"pointer",
				itemclick: function(e){
					if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
						e.dataSeries.visible = false;
					}
					else {
						e.dataSeries.visible = true;
					}
					chart.render();
				}
			},
		});
	chart.render();
})