<?php

require_once 'include/DB_Functions.php';
$db = new DB_Functions(); // tutaj nastepuje łączenie z bazą danych

// json response array
$response = array("error" => FALSE);

// odbieranie danych do wysłania
    $email = $_POST['email'];
	
	$user = $db->getUserPointsByEmail($email);
	
	if($user != false){
		$response["msg"] = "Dane odebrane";
		// user is found
        $response["error"] = FALSE;
		$response["user"]["steps"] = $user["steps"];
		$response["user"]["name"] = $user["name"];
        $response["user"]["email"] = $user["email"];
		$response["user"]["points"] = $user["points"];
		$response["user"]["game"] = $user["game"];
		$response["user"]["poziom"] = $user["poziom"];
		$response["user"]["updated_at"] = $user["updated_at"];
        echo json_encode($response);
}else{
	$response["error"] = TRUE;
    $response["error_msg"] = "Dane nie zostaly odebrane";
    echo json_encode($response);
}
?>