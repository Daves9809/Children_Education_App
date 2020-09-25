<?php

require_once 'include/DB_Functions.php';
$db = new DB_Functions(); // tutaj nastepuje łączenie z bazą danych

// json response array
$response = array("error" => FALSE);

// odbieranie danych do wysłania
    $steps = $_POST['steps'];
	$email = $_POST['email'];
	$updated_at = $_POST['updated_at'];

if($db->updateUserSteps($email, $steps, $updated_at)){
	$response["msg"] = "Dane wyslane";
    echo json_encode($response);
}else{
	$response["error"] = TRUE;
    $response["error_msg"] = "Kroki nie zostaly zaktualizowane, nastapil blad";
    echo json_encode($response);
}
?>