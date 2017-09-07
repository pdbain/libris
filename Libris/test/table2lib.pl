#! /bin/perl
my @fields = (
"f_doctype",
"f_author",
"f_title",
"f_publication",
"f_volume",
"f_issue",
"f_year",
"f_keywords",
"f_publisher",
"f_month",
"f_pages");
my $l;
while ($l = readline(STDIN)) {
	chomp($l);
	print 
"		<record>\n";

	my @f = split(/\t/, $l);
	for my $n (@fields) {
		my $d = shift(@f);
		$d =~ s/\013/\n/g;
		next if ($d eq "");
		print(
"			<field id=\"$n\"");
			if ((length($d) < 16) && (!($d =~ m/"/))) {
				print(" value=\"$d\"/>\n");
			} else {
				$d =~ s/\s*$/\n/;
				print(">\n", $d,
"			</field>\n");
			}
	}
	print(
"		</record>\n");
}