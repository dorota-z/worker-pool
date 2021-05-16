# Workers

Solution by: Dorota Z.

To find original instructions, please see INSTRUCTIONS.md

##Design

Task represents a single job which can succeed, fail or hang. In this project the tasks are not expected to return
result to the caller, that is why the result of the run function is a Unit. If the results were of interest then
a type parameter would have to be added to the task trait.
