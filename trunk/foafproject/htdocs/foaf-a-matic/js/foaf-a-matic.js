/*
 * Description: Javascript FOAF-a-matic implementation
 *
 * Author: Leigh Dodds, leigh@ldodds.com
 *
 * License: Consider this PUBLIC DOMAIN code, do with it what you will, just mention where
 * you got it. Cheers.
 */

/* =========================== Globals ============================= */

gDefaultNumberOfFriends=3;
gCurrentNumberOfFriends=0;
gFriendTableBody = null;

/* =========================== Globals ============================= */

/* =========================== Generate ============================= */

function generate()
{
    if (validate())
    {
        //clear text area
        document.results.rdf.value='';

        gSpamProtect = document.results.spamProtect.checked;

        //process the form values to make a FOAF person
        person = buildPerson();

        //dump the final RDF description
        person.dumpToTextArea(document.results.rdf);
    }
}
/* =========================== Generate ============================= */

/* =========================== Build Person =========================== */
function buildPerson()
{
    p = new Person();

    p.title = document.details.title.value;
    p.firstName = document.details.firstName.value;
    p.surname = document.details.surname.value;
    p.email = document.details.email.value;
    p.nick = document.details.nick.value;
    p.homePage = document.details.homepage.value;
    p.phone = document.details.phone.value;
    p.workplaceHomepage = document.details.workplaceHomepage.value;
    p.workInfoHomepage = document.details.workInfoHomepage.value;
    p.depiction = document.details.depiction.value;
    p.schoolHomepage = document.details.schoolHomepage.value;

    //now add friends
    for (i=1; i<=gCurrentNumberOfFriends;i++)
    {
        if (document.friends.elements['friend_' + i].value != '' && document.friends.elements['friend_' + i + '_mbox'].value != '')
        {
            friend = new Person();
            friend.name=document.friends.elements['friend_' + i].value;
            friend.email=document.friends.elements['friend_' + i + '_mbox'].value;
            friend.seealso=document.friends.elements['friend_' + i + '_seealso'].value;
            p.addFriend(friend);
        }
    }

    return p;
}
/* =========================== Build Person =========================== */

/* ========================== Form Validation ========================== */

//TODO -- validate email addresses

function validate()
{

    isValid = true;
    msg = '';

    if (document.details.firstName.value=='')
    {
        isValid=false;
        msg = msg + field_firstName + '\n';
    }

    if (document.details.surname.value=='')
    {
        isValid=false;
        msg = msg + field_surname + '\n';
    }

    if (document.details.email.value=='')
    {
        isValid=false;
        msg = msg + field_email + '\n';
    }

    for (i=1; i<=gCurrentNumberOfFriends;i++)
    {
        if (document.friends.elements['friend_' + i].value != '' && document.friends.elements['friend_' + i + '_mbox'].value == '')
        {
            isValid=false;
            nameMessage = msg_missingFriendEmail.split('?');
            msg = msg + nameMessage[0] + document.friends.elements['friend_' + i].value;
            msg = msg + nameMessage[1] + '\n';
            //msg = msg + 'An Email Address for ' + document.friends.elements['friend_' + i].value +'\n';
        }
    }

    if (!isValid)
    {
        alert(msg_missingRequired + '\n' + msg);
    }

    return isValid;
}
/* ========================== Form Validation ========================== */

/* ========================== Form Utilities ============================ */

function createFriendFields()
{
    gFriendTableBody = document.getElementById('friendtable');
    for (i=1; i<=gDefaultNumberOfFriends; i++)
    {
        addFriendFields();
    }

    //if we've been referred, then populate first friend
    if (gReferredFriend != null)
    {
        document.friends[0].value = gReferredFriend.friendname;
        document.friends[1].value = gReferredFriend.email;
        document.friends[2].value = gReferredFriend.seealso;
    }
}

function addFriendFields()
{
    gCurrentNumberOfFriends++;
    tr = document.createElement('tr');
    tr.appendChild(addCol(field_friend + '--'));
    tr.appendChild(addCol(field_friendName));
    tr.appendChild(addCol('<input type=\"text\" name=\"friend_'+gCurrentNumberOfFriends+'\" value="">'));
    tr.appendChild(addCol(field_friendEmail));
    tr.appendChild(addCol('<input type=\"text\" name=\"friend_'+gCurrentNumberOfFriends+'_mbox\" value="">'));
    tr.appendChild(addCol(field_friendSeeAlso));
    tr.appendChild(addCol('<input type=\"text\" name=\"friend_'+gCurrentNumberOfFriends+'_seealso\" value="">'));

    gFriendTableBody.appendChild(tr);
}

function addCol(html) {
 var td=document.createElement('td')
 td.innerHTML=html
 return td
}


/* ========================== Form Utilities ============================ */

/* ========================== Refer a Friend ============================ */

gReferredFriend = null;

function ReferredFriend(friendname, surname, email, seealso)
{
    this.friendname= friendname || '';
    this.email = email || '';
    this.seealso=seealso || '';
}

function checkParameters()
{
    rawParameters = document.location.search;

    if (rawParameters == '')
    {
        return;
    }

    rawParametersArray = rawParameters.substring(1).split("&");
    gReferredFriend = new ReferredFriend();

    for (i=0; i<rawParametersArray.length; i++)
    {
        nameAndValue = rawParametersArray[i].split("=");
        if (nameAndValue[0] == 'name')
        {
            gReferredFriend.friendname=unescape(nameAndValue[1]);
        }
        if (nameAndValue[0] == 'email')
        {
            gReferredFriend.email=unescape(nameAndValue[1]);
        }
        if (nameAndValue[0] == 'seealso')
        {
            gReferredFriend.seealso=unescape(nameAndValue[1]);
        }
    }
}

