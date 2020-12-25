%FEATURE_MODEL

% Feature Model v0.7

FEATURES %**********************************************************************
consonantal	con	binary	%  0
sonorant	son	binary	%  1
continuant	cnt	binary	%  2
ejective	eje	binary	%  3
release		rel	binary	%  4
lateral		lat	binary	%  5
nasal		nas	binary	%  6
labial		lab	binary	%  7
round		rnd	binary	%  8
coronal		cor	numeric	%  9
dorsal		dor	binary	% 10
front		frn	ternary	% 11
high		hgh	ternary	% 12
atr			atr	binary	% 13
voice		vce	binary	% 14
creaky		crk	ternary	% 15
breathy		bth	binary	% 16
distributed	dst	binary	% 17
long		lng	binary	% 18
upper		upr	binary	% 19

ALIASES %***********************************************************************
[+vot]    = [+breathy]
[-vot]    = [-breathy]

[high]    = [+high]
[mid]     = [0:high]
[low]     = [-high]

[front]   = [+front]
[central] = [0:front]
[back]    = [-front]

[noncoronal]   = [0:coronal]
[dental]       = [1:coronal]
[alveolar]     = [2:coronal]
[postalveolar] = [3:coronal]
[retroflex]    = [4:coronal, -distributed]
[palatal]      = [4:coronal, +distributed, +front, +high]

CONSTRAINTS %*******************************************************************
% [+nas] and [+lat] cannot co-occur
[+nasal]   > [-lateral]
[+lateral] > [-nasal]

% [-con] and [-son] cannot co-occur
[-sonorant]    > [+consonantal]
[-consonantal] > [+sonorant]

% [continuant] only applies to [+consonantal]
[-continuant]  > [+consonantal]
[-consonantal] > [+continuant]

% Segment can only be [+rel] if it is also [+consonantal, -continuant]
[+release]     > [+consonantal]
[+release]     > [-sonorant]

[-consonantal] > [-release]
[+continuant]  > [-release]
[+sonorant]    > [-release]

% Only obstruents (stops?) can be ejectives
[+ejective] > [+consonantal, -sonorant, -continuant, -voice]

[+sonorant]    > [-ejective]
[-consonantal] > [-ejective]
[+continuant]  > [-ejective]
[+voice]       > [-ejective]

% Distributed requires a closure or near closure
[+distributed] > [+consonantal]
[-consonantal] > [-distributed]

% Obstruents default to ATR for no strong reason
[-sonorant] > [-atr]
[+atr] > [+sonorant]

[+creaky]  > [-breathy, +voice]
[+breathy] > [-creaky]

% Coronal must be high, front
[4:coronal, +distributed] > [+front, +high]

[4:coronal, +distributed, back]    > [3:coronal]
[4:coronal, +distributed, central] > [3:coronal]
[4:coronal, +distributed, mid]     > [3:coronal]
[4:coronal, +distributed, low]     > [3:coronal]
