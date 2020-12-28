<?php

require_once 'include/DB_Functions.php';
$db = new DB_Functions(); // tutaj nastepuje łączenie z bazą danych

// json response array
$response = array("error" => FALSE);


	$email = $_POST['email'];

if($db->resetDataFunction($email)){
	$response["msg"] = "Dane zostaly zresetowane";
    echo json_encode($response);
}else{
	$response["error"] = TRUE;
    $response["error_msg"] = "Dane nie zostaly zresetowane, wystapil blad";
    echo json_encode($response);
}
?>