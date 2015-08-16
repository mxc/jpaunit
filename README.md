#JPAUnit - A library to assist with testing your JPA code

Tired of trying to get DBUnit to work with JPA unit tests? This library
is meant to replace DBUnit and uses DBUnit xml dataset files to load the test 
database by JPA rather than JDBC.

##JPAUnit - Use JPA to set up your unit tests

JPAUnit uses JPA to set up your unit tests. You already have your model defined and
all you need to do is load up and populate entities, persist them and then run your
tests. 

Whats more is that JPAUnit uses your already configured persistence context to load
create the database connections and set up your database for testing. No more
fuzzing around reading up what configuration to change for the testing environment.

With Maven you define a separate persistence.xml with your connection information for
testing using a RESOURCE_LOCAL connection which means you don't need an application
server to run your tests.

### JPAUnit relies on convention

Currently JPAUnit relies on Java POJO naming conventions. It assumes variable and 
function names follow the standard Java naming conventions. The main reason for
this was the requirement to get a usable replacement for DBUnit in our unit tests
as quickly as possible. Since our code follow Java naming standard this was the
quickest way to proceed. The library will be made more flexible in future if 
required. For now it suits our needs nicely!

For entity object it assumes that they all expose their primary key (id) as an 
Integer or int via getId() method.

JPAUnit can read and load DBUnit xml dataset files. It has extensions point to
add new data source parses as the plan is to load the database via JSON in future
instead of the more verbose and harder to parse XML.

A slight difference with DBUnit is that column names should match the Java entities
variable name rather than the database column name. For @OneToMany relationships the
data set file should use the variable name with "_id" appended for reference to the
foreign objects primary key.

Currently JPA can handle field level attributes:

*@OverrideAttributes,
*@Embedded properties,
*Java Enums with our without @Enumerated
*@ManyToMany relationships


###How to use
Include the JPAUnit jar in your application. Then create or use an existing DBUnit
xml file.

`<?xml version="1.0" encoding="UTF-8"?>
<dataset>
    <SimpleStringEntity id="1" stringValue="test1" />
    <SimpleStringEntity id="2" stringValue="test2" />
    <SimpleStringEntity id="3" stringValue="test3" />
</dataset>`

In your unit test call JpaUnit's init method providing it with your dataset file on
the ClassPath, a string for where your Entity classes,or models can be found, and
a parser. Currently we only have a parser for the DBUnit dataset xml files.
`
    private static EntityManager em;
    private final String modelPackageName = "za.co.jumpingbean.jpaunit.test.model";

    @BeforeClass
    public static void beforeClass() {
        em = Persistence.createEntityManagerFactory("jpaunittest").createEntityManager();
    }

    @Test
    public void jpaSimpleStringEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplestringentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        em.clear();
        em.getTransaction().begin();
        try {

            Query qry = em.createQuery(
                    "Select s from SimpleStringEntity s");
.....

        } finally {
            em.getTransaction().commit();
            loader.delete();
        }`

See unit test for more.

##Why JPAUnit?

My Java applications make use of JPA. When it comes to writing unit tests the
choice has been  [Arquillian] (http://arquillian.org/) or [DBUnit]
(http://dbunit.sourceforge.net/). Both of these require some time to master and always
require time to configure for your unit tests.

Arquillian is an acknowledgement that writing unit test for JEE application can't
be done by mocking alone. There are too many moving parts provided by the application
server to make mocking feasible. Although billed as an integration testing 
tool, in my experience, its used to make writing unit test easier. The price is 
configuration complexity and being tied to a particular application server for
testing.

DBUnit implements a lot of features of JPA to enable database independence without
using JPA but changing a database setting requires changes to your persistence xml
and to DBUnit configuration. I can't count how many days I lost to fighting with 
DBUnit to get it to work nicely with different JPA providers

##Why didn't you use x?

I am sure there are other solutions out there, perhaps even better ones. Let us know.
Each year we [Jumping Bean] (https://www.jumpingbean.co.za) release an open source
project for Mandela Day, 18th July. This year (2015) we a bit early. Going to be
busy next week when the day does arrive, so here it is :)
