
##So we want to identify distinct pairs of companies, where there exists some person that is on the board of both companies 


SELECT DISTINCT	o1.personid, directors.firstname, directors.lastname, c1.id, c2.id, c1.companyname, c2.companyname 
FROM		companies as c1, companies as c2, onboard as o1, onboard as o2, directors
WHERE		c1.id != c2.id AND o1.personid=o2.personid and o1.corpid = c1.id and o2.corpid=c2.id AND directors.id = o1.personid ;


