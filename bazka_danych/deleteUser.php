<?php

require_once 'include/DB_Functions.php';
$db = new DB_Functions(); // tutaj nastepuje łączenie z bazą danych

// json response array
$response = array("error" => FALSE);


	$email = $_POST['email'];

if($db->deleteUserFunction($email)){
	$response["msg"] = "Uzytkownik zostal usuniety";
    echo json_encode($response);
}else{
	$response["error"] = TRUE;
    $response["error_msg"] = "Uzytkownik nie zostal usuniety, wystapil blad.";
    echo json_encode($response);
}
?>