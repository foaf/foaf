/*
 * Description: Javascript FOAF helper objects and utilties.
 *
 * Author: Leigh Dodds, leigh@ldodds.com
 *
 * License: Consider this PUBLIC DOMAIN code, do with it what you will, just mention where
 * you got it. Cheers.
 *
 * Dependencies: depends on Paul Johnston's SHA1 Javascript implementation
 *                     see http://pajhome.org.uk/crypt/md5/ for details.
 *
 * $Id: foaf.js,v 1.1 2004-05-21 12:45:46 danbri Exp $
 */

/* =========================== Globals ============================= */

//We doan need no steenkin' globals!
//Um actually we do...

gSpamProtect = false;

/* =========================== Globals ============================= */

/* ======================== Person Object =========================== */
function Person()
{
    //properties
    this.title = '';
    this.firstName = '';
    this.surname = '';
    this.name = '';
    this.nick = '';
    this.email = '';
    this.homePage = '';
    this.workplaceHomepage = '';
    this.workInfoHomepage = '';
    this.schoolHomepage = '';
    this.depiction = '';
    this.phone = '';
    this.seealso = '';

    this.friends = new Array();

    //methods
    this.getName = getName;
    this.mbox = mbox;
    this.mboxSha1 = mboxSha1;
    this.getPhone = getPhone;
    this.dumpToTextArea = dumpToTextArea;
    this.toFOAF = toFOAF;
    this.addFriend = addFriend;
}

function addFriend(person)
{
    this.friends[this.friends.length] = person;
}

function getName()
{
    return (this.name != '' ? this.name : this.firstName + ' ' + this.surname);
}

function mbox()
{
    return 'mailto:' + this.email;
}

function mboxSha1()
{
    return calcSHA1('mailto:' + this.email);
}

function getPhone()
{
    return 'tel:' + this.phone.replace(/ /g, '-');
}

function dumpToTextArea(textarea)
{
    textarea.value = this.toFOAF();
}

function toFOAF()
{
    serializer = new PersonSerializer(this);
    return serializer.getFOAF();
}

/* ======================== Person Object =========================== */

/* ===================== Person Serializer Object ======================= */

//Note: I could have simply built up a DOM tree for the FOAF elements, and
//then serialized this, rather than using this object+methods. However I wasn't
//confident in getting DOM accesses to work cross-browser. I may still do it
//however. It was also easy to port my first code iteration to this structure.

function PersonSerializer(p, merging)
{
    //properties
    this.person = p;
    this.top = '<rdf:RDF\n      xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n      ' +
                  'xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n      ' +
                  'xmlns:foaf=\"http://xmlns.com/foaf/0.1/\">';
    this.tail = '</rdf:RDF>';
    this.merging = merging || false;

    //methods
    this.getFOAF = getFOAF;
    this.makePerson = makePerson;
    this.makeName = makeName;
    this.makeNick = makeNick;
    this.makeMbox = makeMbox;
    this.makeHome = makeHome;
    this.makeWorkHome = makeWorkHome;
    this.makeWorkInfo = makeWorkInfo;
    this.makeSchoolHome = makeSchoolHome;
    this.makePhone = makePhone;
    this.makeDepiction = makeDepiction;
    this.makeFriends = makeFriends;
    this.makeSeeAlso = makeSeeAlso;
}

function getFOAF()
{
    return (this.merging ? this.makePerson() : this.top + this.makePerson() + '\n' + this.tail);
}

function makePerson()
{
    //merging this person into the main description? if so, wrap in a knows element.
    if (this.merging)
    {
        return makeSimpleTag('foaf', 'knows', makeSimpleTag('foaf', 'Person', this.makeName() +
                                                           this.makeNick() +
                                                           this.makeMbox() +
                                                           this.makeHome() +
                                                           this.makeDepiction() +
                                                           this.makePhone() +
                                                           this.makeWorkHome() +
                                                           this.makeWorkInfo() +
                                                           this.makeSchoolHome() +
                                                           this.makeSeeAlso() +
                                                           this.makeFriends())
                                    );

    }

    return makeSimpleTag('foaf', 'Person', this.makeName() +
                                                       this.makeNick() +
                                                       this.makeMbox() +
                                                       this.makeHome() +
                                                       this.makeDepiction() +
                                                       this.makePhone() +
                                                       this.makeWorkHome() +
                                                       this.makeWorkInfo() +
                                                       this.makeSchoolHome() +
                                                       this.makeFriends());
}

function makeFriends()
{
    var friends = '';

    if (this.person.friends.length == 0)
    {
        return friends;
    }

    for (i=0; i<this.person.friends.length;i++)
    {
        serializer = new PersonSerializer(this.person.friends[i], true);
        friends = friends + serializer.getFOAF();
    }
    return friends;
}

function makeSeeAlso()
{
    if (this.person.seealso == '')
    {
        return '';
    }

    return makeRDFResourceTag('rdfs', 'seeAlso', this.person.seealso);
}

function makeName()
{
    return makeSimpleTag('foaf', 'name', this.person.getName()) +
             (this.person.title == '' ? '' : makeSimpleTag('foaf', 'title', this.person.title) ) +
             (this.person.firstName == '' ? '' : makeSimpleTag('foaf', 'firstName', this.person.firstName)) +
             (this.person.surname == '' ? '' : makeSimpleTag('foaf', 'surname', this.person.surname));
}

function makeNick()
{
    if (this.person.nick == '')
    {
        return '';
    }
    return makeSimpleTag('foaf', 'nick', this.person.nick);
}

function makeMbox()
{
    if (this.person.email == '')
    {
        return '';
    }
    if (gSpamProtect)
    {
        return makeSimpleTag('foaf', 'mbox_sha1sum', this.person.mboxSha1());
    }
    return makeRDFResourceTag('foaf', 'mbox', this.person.mbox());
}

function makePhone()
{
    if (this.person.phone == '')
    {
        return '';
    }

    return makeRDFResourceTag('foaf', 'phone', this.person.getPhone());
}

function makeHome()
{
    if (this.person.homePage == '')
    {
        return '';
    }

    return makeRDFResourceTag('foaf', 'homepage', this.person.homePage);
}

function makeWorkHome()
{
    if (this.person.workplaceHomepage == '')
    {
        return '';
    }
    return makeRDFResourceTag('foaf', 'workplaceHomepage', this.person.workplaceHomepage);
}

function makeWorkInfo()
{
    if (this.person.workInfoHomepage == '')
    {
        return '';
    }
    return makeRDFResourceTag('foaf', 'workInfoHomepage', this.person.workInfoHomepage);
}

function makeDepiction()
{
    if (this.person.depiction == '')
    {
        return '';
    }
    return makeRDFResourceTag('foaf', 'depiction', this.person.depiction);
}

function makeSchoolHome()
{
    if (this.person.schoolHomepage == '')
    {
        return '';
    }
    return makeRDFResourceTag('foaf', 'schoolHomepage', this.person.schoolHomepage);
}

/* ===================== Person Serializer Object ======================= */

/* ====================== XML Utility Methods ========================= */

function makeRDFResourceTag(prefix, localname, resource)
{
    return  '\n' + '<' + prefix + ':' + localname + ' rdf:resource=\"' + resource + '\"/>';
}

function makeSimpleTag(prefix, localname, contents)
{
    return  '\n' + makeOpeningTag(prefix, localname) + contents + makeClosingTag(prefix, localname);
}

function makeOpeningTag(prefix, localname)
{
    return '<' + prefix + ':' + localname + '>';
}

function makeClosingTag(prefix, localname)
{
    return '</' + prefix + ':' + localname + '>';
}
/* ====================== XML Utility Methods ========================= */