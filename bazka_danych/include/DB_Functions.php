<?php

/**
 * @author Ravi Tamada
 * @link http://www.androidhive.info/2012/01/android-login-and-registration-with-php-mysql-and-sqlite/ Complete tutorial
 */

class DB_Functions {

    private $conn;

    // constructor, ktory realizuje polaczenie z baza danych
    function __construct() {
        require_once 'DB_Connect.php';
        // connecting to database
        $db = new Db_Connect();
        $this->conn = $db->connect();
    }

    // destructor
    function __destruct() {
        
    }

    /**
     * Storing new user
     * returns user details
     */
    public function storeUser($name, $email, $password) {
        $uuid = uniqid('', true);
        $hash = $this->hashSSHA($password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt

        $stmt = $this->conn->prepare("INSERT INTO users(unique_id, name, email, encrypted_password, salt, poziom, points, created_at, updated_at) VALUES(?, ?, ?, ?, ?, '1', '0', NOW(), UTC_DATE())");
        $stmt->bind_param("sssss", $uuid, $name, $email, $encrypted_password, $salt);
        $result = $stmt->execute();
        $stmt->close();

        // check for successful store
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM users WHERE email = ?");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $user;
        } else {
            return false;
        }
    }
	
	public function changePasswordFunction($email, $password){
		$hash = $this->hashSSHA($password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt
		
		$stmt = $this->conn->prepare("UPDATE users SET encrypted_password=?,salt=? WHERE email=?");
        $stmt->bind_param("sss", $encrypted_password, $salt, $email);
		
        if ($stmt->execute()) { 
			$stmt->close(); 
			return true; 
		}
		else {
			$stmt->close(); 
			return false; 
			}
	}
	
	public function changeNameFunction($email, $name){
		
		$stmt = $this->conn->prepare("UPDATE users SET name=? WHERE email=?");
        $stmt->bind_param("ss", $name, $email);
		
        if ($stmt->execute()) { 
			$stmt->close(); 
			return true; 
		}
		else {
			$stmt->close(); 
			return false; 
			}
	}
	public function deleteUserFunction($email){
		
		$stmt = $this->conn->prepare("DELETE FROM users WHERE email=?");
        $stmt->bind_param("s", $email);
		
        if ($stmt->execute()) { 
			$stmt->close(); 
			return true; 
		}
		else {
			$stmt->close(); 
			return false; 
			}
	}
	
	public function resetDataFunction($email){
		
		$stmt = $this->conn->prepare("UPDATE users SET points=0, steps=0, game=0, poziom =1 WHERE email=?");
        $stmt->bind_param("s", $email);
		
        if ($stmt->execute()) { 
			$stmt->close(); 
			return true; 
		}
		else {
			$stmt->close(); 
			return false; 
			}
	}
	
	//funkcja do aktualizowania kroków użytkownika
	public function updateUserSteps($email, $steps, $updated_at,$points,$game,$poziom) {
		$stmt = $this->conn->prepare("UPDATE users SET steps=?,updated_at=?,points =?,game =?,poziom =? WHERE email=?");
		$stmt->bind_param("ssssss", $steps, $updated_at,$points,$game,$poziom,$email);
		if ($stmt->execute()) { 
			$stmt->close(); 
			return true; 
		}
		else {
			$stmt->close(); 
			return false; 
			}
	}

    /**
     * Get user by email and password
     */
    public function getUserByEmailAndPassword($email, $password) {

        $stmt = $this->conn->prepare("SELECT * FROM users WHERE email = ?");

        $stmt->bind_param("s", $email);

        if ($stmt->execute()) {
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            // verifying user password
            $salt = $user['salt'];
            $encrypted_password = $user['encrypted_password'];
            $hash = $this->checkhashSSHA($salt, $password);
            // check for password equality
            if ($encrypted_password == $hash) {
                // user authentication details are correct
                return $user;
            }
        } else {
            return NULL;
        }
    }
	
	public function getUserPointsByEmail($email){
		$stmt = $this->conn->prepare("SELECT * FROM users WHERE email = ?");
		$stmt->bind_param("s", $email);
		if ($stmt->execute()) {
			$user = $stmt->get_result()->fetch_assoc();
            $stmt->close();
			return $user;
		}else{
			return NULL;
		}
	}
	public function getAllUsers(){
		$stmt = $this->conn->prepare("SELECT name, poziom, points FROM users ");
		$stmt ->execute();
		$stmt ->bind_results("$name, $poziom, $points");

		$users = array();

		while($stmt ->fetch()){
			
			$temp = array();
			
			$temp['name'] = $name;
			$temp['poziom'] = $poziom;
			$temp['points'] = $points;
			
			array_push($users, $temp);
		}

		echo json_encode($users);
	}
	
	

    /**
     * Check user is existed or not
     */
    public function isUserExisted($email) {
        $stmt = $this->conn->prepare("SELECT email from users WHERE email = ?");

        $stmt->bind_param("s", $email);

        $stmt->execute();

        $stmt->store_result();

        if ($stmt->num_rows > 0) {
            // user existed 
            $stmt->close();
            return true;
        } else {
            // user not existed
            $stmt->close();
            return false;
        }
    }

    /**
     * Encrypting password
     * @param password
     * returns salt and encrypted password
     */
    public function hashSSHA($password) {

        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }

    /**
     * Decrypting password
     * @param salt, password
     * returns hash string
     */
    public function checkhashSSHA($salt, $password) {

        $hash = base64_encode(sha1($password . $salt, true) . $salt);

        return $hash;
    }

}

?>
