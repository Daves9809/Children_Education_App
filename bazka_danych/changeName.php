<?php

require_once 'include/DB_Functions.php';
$db = new DB_Functions(); // tutaj nastepuje rowniez logowanie do bazy danych

// json response array
$response = array("error" => FALSE);

    $email = $_POST['email'];
    $name = $_POST['name'];
		$response["msg"] = "proba";
		echo json_encode($response);
        
    if($db->changeNameFunction($email,$name)){
		$response["msg"] = "Imie zmienione";
		echo json_encode($response);
	}else{
		$response["error"] = TRUE;
		$response["error_msg"] = "Imie nie zostalo zmienione, nastąpił blad w php";
		echo json_encode($response);
}
?>

