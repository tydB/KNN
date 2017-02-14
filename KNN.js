function distance(a, b) { // does not do one dimensional yet
	if (a.length != b.length) {
		console.log("Mismatched lengths.", a, b);
		return -1;
	}
	// console.log("Calc Distance: ", a, b);
	var dist = 0;
	for (var i = 0; i < a.length; i++) {
		dist += Math.pow(a[i] - b[i], 2);
	}
	return Math.sqrt(dist);
}
function averageList(list) {
	var average = [];
	var l = list.length;
	if (list.length == 0) {
		console.log("Nothing to average");
		return null;
	}
	for (var i = 0; i < list[0].length; i++) {
		average[i] = 0;
	}
	for (var i = 0; i < list.length; i++) {
		for (var j = 0; j < list[i].length; j++) {
			average[j] += list[i][j];
		}
	}
	for (var i = 0; i < average.length; i++) {
		average[i] /= l;
	}
	return average;
}
function KNN(list, passes, groups) {
	if (!list[0].length) {
		for (var i = 0; i < list.length; i++) {
			var temp = list[i];
			list[i] = [];
			list[i][0] = temp;
		}
	}
	var index = 0;
	var averages = [];
	var clusters = [];
	for (var p = 0; p < passes; p++) {
		if (p == 0) {
			// init clusters to arrays
			for (var i = 0; i < groups; i++) {
				clusters[i] = [];
				clusters[i][0] = list[index++];
				averages[i] = clusters[i][0];
			}
		}
		else {
			for (var i = 0; i < groups; i++) {
				clusters[i] = [];
			}
		}
		while (index < list.length) {
			var c = list[index++];
			var dist = -1;
			var selected = 0;
			for (var i = 0; i < averages.length; i++) {
				var temp = distance(averages[i], c);
				if (temp < dist || dist == -1) {
					dist = temp;
					selected = i;
				}
			}
			clusters[selected].push(c);
			averages[selected] = averageList(clusters[selected]);
		}
		index = 0;
	}
	console.log("Clusters: ", clusters);
	console.log("Averages: ", averages);
}
function main() {
	var data = [1,3,6,4,3,7,9,8,1,2];
	// var data = [[1,3],[5,8],[8,1],[2,7]];
	// var data = randomData(100, 10, 4);
	KNN(data, 2, 2);
}
main();
function randomData(max, size, dims) {
	var data = [];
	for (var i = 0; i < size; i++) {
		data[i] = [];
		for (var j = 0; j < dims; j++) {
			data[i][j] = Math.floor(Math.random() * max);
		}
	}
	return data;
}