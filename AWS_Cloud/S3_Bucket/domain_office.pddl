(define (domain office)
(:requirements :strips)
(:predicates (temperature_humidity ?t)
             (brightness ?b)
             (coffee ?c)
             (at-office ?o))

	(:action fan
		:parameters  (?from ?to)
		:precondition (and  (temperature_humidity ?from))
		:effect (and  (temperature_humidity ?to) (not (temperature_humidity ?from))))
		
	(:action light
		:parameters (?from ?to)
		:precondition  (and (brightness ?from))
		:effect (and (brightness ?to) (not (brightness ?from))))
 
	(:action c_state
		:parameters  (?from ?to)
		:precondition  (and  (coffee ?from))
	    :effect (and (coffee ?to) (not (coffee ?from))))
		
	(:action move
	    :parameters (?from ?to)
	    :precondition (and (at-office?from))
	    :effect (and (at-office ?to) (not (at-office ?from))))
)