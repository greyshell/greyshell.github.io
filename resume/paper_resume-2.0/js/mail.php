<?php

$name = $_POST['name'];
$last = $_POST['last'];
$email = $_POST['email'];
$subject = $_POST['subject'];
$text = stripslashes($_POST['message']);

$to = "jon@gmail.com"; // your email address goes here

$message = $name . " wrote:\n\n";
$message .= $text;

$headers = 'From: ' . $name . ' <' . $email . '>' . "\r\n" .
    		  'Reply-To: ' . $name . ' <' . $email . '>' . "\r\n" .
		     'X-Mailer: PHP/' . phpversion();

if(!empty($name) and empty($last) and !empty($email) and !empty($subject) and !empty($text)) {
	if(mail($to, $subject, $message, $headers, "-f".$email)) {
		echo json_encode(array('error'=>'false','msg'=>'Email sent. Thank you.'));
	} else {
		echo json_encode(array('error'=>'true','msg'=>'There was an error. Please try again.'));
	}
} else {
	echo json_encode(array('error'=>'true','msg'=>'There was an error. Please try again.'));
}
?>