package org.k3x.vemprarua.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;


public class RequisitionException {

	public static int EXCEPTION;
	private static ProgressDialog dialog;
	private static Context context;


	public static void setContext(Context context) {
		RequisitionException.context = context;
	}

	public static void setError(int exception) {
		RequisitionException.EXCEPTION =  exception;
	}

	public static AlertDialog.Builder buildErrorDialog (AlertDialog.Builder builder, Context context){
		RequisitionException.context = context;

		//took too much time to execute the requisition. should try again
		if (EXCEPTION == JsonREST.EXCEPTION_TIMEOUT ){
			builder.setTitle("Falha de conexão!").setMessage("Clique para tentar novamente!")
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					tryAgain();
				}
			});

			//Probably is out of internet connection. try again ll not be usefull
		} else if (EXCEPTION == JsonREST.EXCEPTION_INTERNET ){
			builder.setTitle("Erro!").setMessage("Erro de conexão!")
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			//cannot convert the response to json object. there is nothing to do but try to solve the error programmatically
		} else if (EXCEPTION == JsonREST.EXCEPTION_JSON ){
			builder.setTitle("Erro!").setMessage("Erro interno!")
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			//the server returned a status != 200. there is nothing to do but try to solve the error programmatically
		} else if (EXCEPTION == JsonREST.EXCEPTION_ILLEGAL ){
			builder.setTitle("Erro!").setMessage("Erro interno")
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});
		}

		setError(JsonREST.EXCEPTION_NONE);
		return builder;
	}


	public static void tryAgain(){
		dialog = ProgressDialog.show( RequisitionException.context, "", "Tente novamente!", true);

		JsonREST json = new JsonREST();
		json.tryAgain();
	}

	public static void dismiss() {
		if (dialog == null) return;

		if (dialog.isShowing()){
			dialog.dismiss();
		}
	}


	public static void buildInfoMessage(Context context, String title, String msg){
		AlertDialog.Builder builder = new AlertDialog.Builder(context); 
		if (EXCEPTION == JsonREST.EXCEPTION_NONE){
			builder.setTitle(title).setMessage(msg).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.cancel();
				}
			});
		} else {
			builder = buildErrorDialog(builder, context);
		}
		builder.create().show();
	}

}
