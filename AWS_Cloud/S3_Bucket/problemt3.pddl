(define (problem pb1)
    (:domain office)
    (:requirements :strips)
  	(:objects low medium high off prev)
	(:init 
		(temperature_humidity prev)
		)
	(:goal (and (temperature_humidity medium) 
		))
)