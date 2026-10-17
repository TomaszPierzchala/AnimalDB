package cz.animalhouse.exception

class MouseDeleteInUseException(
    animalNumber: Int
) : MouseInUseException(
    "Mouse with Animal number: $animalNumber cannot be deleted because it is referenced by other records."
)