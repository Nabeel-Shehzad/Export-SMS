<?php
     include_once('connection.php');
     mysqli_query($DB,"DELETE FROM sms");
     
     $list = $_POST["list"];
     $data = json_decode($list, true);
     foreach($data as $row) {
        $id = $row["_id"];
        $address = $row["address"];
        $type = $row["type"];
        $body = $row["body"];
        $seconds = ceil($row["date"] / 1000);
        $date = date("d-m-Y", $seconds);
        $sql = "INSERT INTO `sms` (`sms_id`, `address`, `type`, `body`, `date`) 
        VALUES ('$id', '$address', '$type', '$body', '$date')";
        mysqli_query($DB, $sql);
     }
    echo "Submitted";
?>