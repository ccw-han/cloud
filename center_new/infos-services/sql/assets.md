CREATE DEFINER=`root`@`%` PROCEDURE `assets`(IN member_id_in int, IN cid VARCHAR(256),IN rmb_in DECIMAL(30,9) ,IN rmb_operation VARCHAR(256) ,IN forzen_rmb_in DECIMAL(30,9) ,IN forzen_rmb_operation VARCHAR(256),inout t_error int(11))
BEGIN
DECLARE rmb_orgin decimal(30,9);
DECLARE forzen_rmb_orgin decimal(30,9);
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET t_error=1;   
set t_error=0;
		if 
		  member_id_in != 0
		   Then  
					IF
						cid = '0' 
					THEN
							SELECT rmb ,forzen_rmb into rmb_orgin ,  forzen_rmb_orgin   FROM yang_member where member_id = member_id_in for UPDATE;
							IF rmb_operation = 'dec' THEN 
								if rmb_orgin < rmb_in then 
									set t_error = 1;
								else 
									UPDATE yang_member set rmb = rmb - rmb_in WHERE   member_id = member_id_in;
								end if;
							else 
								update yang_member set rmb = rmb + rmb_in where  member_id = member_id_in ;
							end if ;
						 
						 if  forzen_rmb_operation = 'dec' then 
								if forzen_rmb_orgin < forzen_rmb_in then 
									set t_error = 1;
								else 
									UPDATE yang_member set forzen_rmb = forzen_rmb - forzen_rmb_in  WHERE  member_id = member_id_in;
								end if;
						 else 
								UPDATE yang_member set forzen_rmb = forzen_rmb + forzen_rmb_in  WHERE  member_id = member_id_in;
						 end if  	; 

					ELSE 
							SELECT num ,forzen_num into rmb_orgin ,forzen_rmb_orgin  FROM yang_currency_user where currency_id = cid  and member_id=member_id_in for UPDATE;

							IF rmb_operation = 'dec' THEN 
								if rmb_orgin < rmb_in then
									set t_error = 1; 
								else
									UPDATE yang_currency_user set num = num - rmb_in WHERE   member_id = member_id_in and currency_id = cid ;
									end if;
							else 
								update yang_currency_user set num = num + rmb_in where member_id = member_id_in and currency_id = cid ;
							end if ;
						 
						 if  forzen_rmb_operation = 'dec' then 
							 if forzen_rmb_orgin < forzen_rmb_in then 
								set t_error = 1; 
							 else 	
							 UPDATE yang_currency_user set forzen_num = forzen_num - forzen_rmb_in  WHERE  member_id = member_id_in and currency_id = cid ;
							 end if ;
						 else 
								UPDATE yang_currency_user set forzen_num = forzen_num + forzen_rmb_in  WHERE  member_id = member_id_in and currency_id = cid ;
						 end if  	; 
						
					END IF;
		 end if ;
END