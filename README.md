NoDTO
=====

NoDTO allows you to avoid using DTO objects and help overcome serialization issues in Java. 
One of the most prominent applications of the library is to overcome [GWT and Hibernate][1] problem. If you are not familiar with the problem, here is the brief introduction: 

 1. Imagine you have 2 objects. ObjectA is of ClassA, ObjectB is of ClassB
 2. ClassA has field named `reference` of type ClassB and getter `public ClassB getReference() { return reference; }`
 3. You use Hibernate (or other ORM library) to store the objects in the database.
 4. Imagine you want to load ObjectA from the database and assign it to objectA variable.
 5. When your ORM library loads ObjectA from database, very often "Lazy Loading" technique is used. In such case ObjectB is not loaded immediately. But what value should ORM library put in field `reference` ? Lazy Loading usually means that there is special "Proxy" object is created and put in `reference`. The Proxy object has the same interface as ClassB, but it's quite different from your original ObjectB.
 6. As soon as you hit any method of ClassB (which is represented by Proxy Object, not by ObjectB), the Proxy loads the ObjectB from the database and redirects your call to the ObjectB method.
 7. But the Proxy object is not serializable! And if you ask GWT to send ObjectA from server side over to client side, everything explodes when GWT RPC mechanism encounters the Proxy.

Very often "Data Transfer Object" (DTO) approach is taken where you have to either create new class that will transfer primitive values over the wire. In such case you will need to copy over values from ObjectA into the DTO object. Most of the time this is done manually. This is a lot of boilerplate! You can re-use ClassA of cause, but copying the values is still required. 

NoDTO takes care of copying the values for you. Not only for the given object, but for whole hierarchy! And collections are also covered! You simply tell NoDTO what serialization path should be taken, and it will follow the path, create DTO of the same type as your original object, and copy values automatically. Becareful when you specify the serialization path though!

  [1]: http://www.gwtproject.org/articles/using_gwt_with_hibernate.html
