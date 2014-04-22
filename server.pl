#!/usr/bin/perl

use DBI;
use strict;
use IO::Socket::INET;

$|++;

my $sock = IO::Socket::INET -> new(
  LocalPort => 1337,
  Listen => 1,
  Proto => 'tcp',
  Timeout => 30
) or die "[!] Error while creating the socket: $!\n";

my ($username, $password) = ("ocean", "digital");

print "[*] Server starting.\n";
my $dbh = DBI->connect('DBI:mysql:sales_db', 'ocean', 'digital', { RaiseError => 1, AutoCommit => 1} ) or die "Connection error: $DBI::errstr\n";

sub add{ # Dont need to strip special characters because of the '?'
  my $date = time();
  my ($name, $addr, $city, $state, $zip, $phone, $cell, $email, $type, $model, $serial, $service, $adapt, $cond, $soft, $os, $desc, $comm) = split(":::::", $_[0]);
  my $idh = $dbh -> prepare('select id from id');
  $idh -> execute();
  @_ = $idh->fetchrow_array();
  my $id = $_[0];
  my $sth = $dbh -> prepare('insert into customers values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)');
  $sth -> execute($id, $name, $date, $addr, $city, $state, $zip, $phone, $cell, $email, $type, $model, $serial, $service, $adapt, $cond, $soft, $os, $desc, $comm) or warn "Error adding data: $dbh->errstr\n";
  $idh = $dbh -> prepare('update id set id = id + 1');
  $idh -> execute();
  $sth -> finish;
  return $id;
}

sub del{
  my $id = shift;
  my $sth = $dbh -> prepare('delete from customers where id = ?');
#  $id =~ s/.$//; # Because chomp doesnt work?
#  print "ID was: $id\n";
  $sth -> execute($id);
}

sub usage{
  my $client = shift;
  print $client "Three Stone Solutions (c) Database server.\n";
  print $client "!edit [field #] [search] [replacement]\n";
  print $client "!list [field #] [search]\n";
  print $client "!help - This menu.\n";
  print $client "Field numbers are:";
  print $client qq(
    0 - id
    1 - name
    2 - date
    3 - address
    4 - city
    5 - state
    6 - zip
    7 - phone
    8 - cell
    9 - email
    10 - type
    11 - brand
    12 - serial
    13 - service
    14 - laptop adaptor
    15 - condition
    16 - software
    17 - OS
    18 - description
    19 - comments
  );
}

sub edit{ # Edit one field at a time.
  my ($id, $pick, $replace) = @_;
  my $sth;
  if($pick == 1){
    $sth = $dbh -> prepare('update customers set name = ? where id = ?');
  }
  elsif($pick == 2){
    $sth = $dbh -> prepare('update customers set date = ? where id = ?');
  }
  elsif($pick == 3){
    $sth = $dbh -> prepare('update customers set address = ? where id = ?');
  }
  elsif($pick == 4){
    $sth = $dbh -> prepare('update customers set city = ? where id = ?');
  }
  elsif($pick == 5){
    $sth = $dbh -> prepare('update customers set state = ? where id = ?');
  }
  elsif($pick == 6){
    $sth = $dbh -> prepare('update customers set zip = ? where id = ?');
  }
  elsif($pick == 7){
    $sth = $dbh -> prepare('update customers set phone = ? where id = ?');
  }
  elsif($pick == 8){
    $sth = $dbh -> prepare('update customers set cell = ? where id = ?');
  }
  elsif($pick == 9){
    $sth = $dbh -> prepare('update customers set email = ? where id = ?');
  }
  elsif($pick == 10){
    $sth = $dbh -> prepare('update customers set type = ? where id = ?');
  }
  elsif($pick == 11){
    $sth = $dbh -> prepare('update customers set brand = ? where id = ?');
  }
  elsif($pick == 12){
    $sth = $dbh -> prepare('update customers set serial = ? where id = ?');
  }
  elsif($pick == 13){
    $sth = $dbh -> prepare('update customers set service = ? where id = ?');
  }
  elsif($pick == 14){
    $sth = $dbh -> prepare('update customers set laptop_ad = ? where id = ?');
  }
  elsif($pick == 15){
    $sth = $dbh -> prepare('update customers set cond = ? where id = ?');
  }
  elsif($pick == 16){
    $sth = $dbh -> prepare('update customers set software = ? where id = ?');
  }
  elsif($pick == 17){
    $sth = $dbh -> prepare('update customers set os = ? where id = ?');
  }
  elsif($pick == 18){
    $sth = $dbh -> prepare('update customers set description = ? where id = ?');
  }
  elsif($pick == 19){
    $sth = $dbh -> prepare('update customers set comments = ? where id = ?');
  }
  $replace =~ s/.$//;
#  print "Replacing ID: $id field #$pick new: $replace\n";
  $sth -> execute($replace, $id);
  $sth -> finish;
}

sub list{ # Only search by one item?
  my $client = shift;
  my ($pick, $search) = @_; # A number to let it know what youre looking for. ie. the field.
  my $sth;
  if($pick == 0){
    $sth = $dbh -> prepare('select * from customers where id like ?');
  }
  elsif($pick == 1){
    $sth = $dbh -> prepare('select * from customers where name like ?');
  }
  elsif($pick == 2){
    $sth = $dbh -> prepare('select * from customers where date like ?');
  }
  elsif($pick == 3){
    $sth = $dbh -> prepare('select * from customers where address like ?');
  }
  elsif($pick == 4){
    $sth = $dbh -> prepare('select * from customers where city like ?');
  }
  elsif($pick == 5){
    $sth = $dbh -> prepare('select * from customers where state like ?');
  }
  elsif($pick == 6){
    $sth = $dbh -> prepare('select * from customers where zip like ?');
  }
  elsif($pick == 7){
    $sth = $dbh -> prepare('select * from customers where phone like ?');
  }
  elsif($pick == 8){
    $sth = $dbh -> prepare('select * from customers where cell like ?');
  }
  elsif($pick == 9){
    $sth = $dbh -> prepare('select * from customers where email like ?');
  }
  elsif($pick == 10){
    $sth = $dbh -> prepare('select * from customers where type like ?');
  }
  elsif($pick == 11){
    $sth = $dbh -> prepare('select * from customers where brand like ?');
  }
  elsif($pick == 12){
    $sth = $dbh -> prepare('select * from customers where serial like ?');
  }
  elsif($pick == 13){
    $sth = $dbh -> prepare('select * from customers where service like ?');
  }
  elsif($pick == 14){
    $sth = $dbh -> prepare('select * from customers where laptop_ad like ?');
  }
  elsif($pick == 15){
    $sth = $dbh -> prepare('select * from customers where cond like ?');
  }
  elsif($pick == 16){
    $sth = $dbh -> prepare('select * from customers where software like ?');
  }
  elsif($pick == 17){
    $sth = $dbh -> prepare('select * from customers where os like ?');
  }
  elsif($pick == 18){
    $sth = $dbh -> prepare('select * from customers where description like ?');
  }
  elsif($pick == 19){
    $sth = $dbh -> prepare('select * from customers where comments like ?');
  }
  chomp $search;
  $search =~ s/.$//;
  $search =~ s/(.*)/%$1%/;
  $sth -> execute($search);
  while(@_ = $sth->fetchrow_array()){
    print $client "@_\n";
  }
  if($sth -> rows == 0){
    print $client "[*] Nothing matched your search. Please try again with another keyword.\n";
  }
  $sth -> finish;  
}

sub get{
	my $client = shift;
	my $sth = $dbh -> prepare('select * from customers');
	$sth -> execute();
	while(@_ = $sth->fetchrow_array()){
		print $client join(':::::', @_) . "\n";
	}
	print $client "\n"; # Terminate the in.readLine() from the client.
}

sub login{
	my $s = shift;
	my $user = <$s>;
	my $pass = <$s>;
	$user =~ s/\s+$// unless(!$user); # Get rid of the \r\n
	$pass =~ s/\s+$// unless(!$pass);
	close $s if($s && ($username ne $user || $password ne $pass));
}

# Main program
#my $pid;
#if($pid=fork()){
#  print "[*] Server running.\n";
#  exit;
#}
#elsif($pid < 0){
#  die "[!] Error forking off the server: $!\n";
#}
while(1){
  my $client = $sock -> accept();
  login($client);
  while(<$client>){
#    print; # For debugging use.
    chomp;
    if(/^!edit/){
      s/^!edit\s*//;
      /^(\d+) (\d+) (.*)$/;
      if(!(defined $1 && defined $2 && defined $3)){
        print $client "Usage is: !edit [id #] [field #] [replacement]\n";
      }
      else{
        edit($1, $2, $3);
      }
    }
    elsif(/^!list/){
      s/^!list\s*//;
      /(\d+) (.*)/;
      if(!(defined $1 && defined $2)){
        print $client "Usage is: !list [field #] [replacement] [id]\n";
      }
      else{
        list($client, $1, $2);
      }
    }
    elsif(/^!add/){ # !add [fields 0-19] use ::::: for the delimiter
      s/^!add\s*//;
      print $client add($_) . "\n";
    }
    elsif(/^!del/){
      s/^!del\s*//;
      /(\d+)/;
      if(!defined $1){
        print $client "Usage is: !del [id #]\n";
      }
      else{
        del($1);
      }
    }
    elsif(/^!help/){
      usage($client);
    }
    elsif(/^!get/){
    	get($client);
    }
  }
}

$dbh->disconnect;
