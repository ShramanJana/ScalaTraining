// Question1:
//     Implement Muliple inhertance with traits
   
// Trait GetStarted --> has implemented method prepare(also check if it is abstract)
// Trait Cook extends GetStarted --> has implemented method prepare (super.prepare call must be there)
// Trait Seasoning ---> has implemented method applySeasoning
// Class Food extends Cook with Seasoning --> Has implemented method prepareFood
//           that calls prepare and Seasoning

//           the order of execution must be

//           GetStarted prepare
//           Cook prepare
//           Seasoning applySeasoning

// Check whether there is need of abstract override and comment why in the submisstion

trait GetStarted {
    def prepare(): String = "GetStarted prepare"
}
trait KeepIngredients extends GetStarted {
    override def prepare(): String = {
        println(super.prepare())
        "KeepIngredients prepare"
    }
}
trait Cook extends KeepIngredients {
    override def prepare(): String = {
        println(super.prepare())
        "Cook prepare"
    }
    
}

trait Seasoning {
    def applySeasoning() = {
        println("Seasoning applySeasoning")
    }
}

class Food extends Cook with Seasoning {
    
    override def prepare(): String = {
        super.applySeasoning()
        super.prepare()
        "Food prepare"
    }
}
@main def TraitExercise1 = {
    val obj: GetStarted = new Food()
    println(obj.prepare())
}

// Have only used the override keyword only to override the function prepare, no need of abstract keyword


// ====Output=====
// Seasoning applySeasoning
// GetStarted prepare
// KeepIngredients prepare
// Food prepare