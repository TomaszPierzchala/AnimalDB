package cz.animalhouse.exception

class MouseSexChangeInUseException(
    animalNumber: Int
) : MouseInUseException(
    "Sex of mouse $animalNumber cannot be changed because the mouse is used as a parent."
)