<?php

		require_once (dirname(dirname(FILE)). '/include/DB_Connect.php');
		// connecting to database
        $db = new Db_Connect();
        $conn = $db->connect();

		$stmt = $conn->prepare("SELECT name, poziom, points FROM users ");
		$stmt->execute();
		$stmt->bind_result($name, $poziom, $points);

		$users = array();

		while($stmt->fetch()){
			
			$temp = array();
			
			$temp['name'] = $name;
			$temp['poziom'] = $poziom;
			$temp['points'] = $points;
			
			array_push($users, $temp);
		}

		echo json_encode($users);
	

	/*$response["error"] = TRUE;
    $response["error_msg"] = "Użytkownicy nie zostali pobrani z bazy danych";
    echo json_encode($response);*/


?>
