package pd.andrdmon;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pratikdhiman on 5/3/16.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> {

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView processName, processId, processStarttime, processTotalMem, processRamMem, dataSend, dataRec, dataTotal;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            processName = (TextView) itemView.findViewById(R.id.process_name);
            processId = (TextView) itemView.findViewById(R.id.process_id);
            processStarttime = (TextView) itemView.findViewById(R.id.process_starttime);
            processTotalMem = (TextView) itemView.findViewById(R.id.process_total_mem);
            processRamMem = (TextView) itemView.findViewById(R.id.process_ram_mem);
            dataSend = (TextView) itemView.findViewById(R.id.data_send);
            ;
            dataRec = (TextView) itemView.findViewById(R.id.data_rec);
            dataTotal = (TextView) itemView.findViewById(R.id.data_total);
        }
    }

    List<ProcessInfo> processList;

    RVAdapter(List<ProcessInfo> proList) {
        processList = proList;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        personViewHolder.processName.setText(processList.get(i).pname);
        personViewHolder.processId.setText(processList.get(i).pid + " ");
        personViewHolder.processStarttime.setText(processList.get(i).starttime);
        personViewHolder.processTotalMem.setText(processList.get(i).totalmem);
        personViewHolder.processRamMem.setText(processList.get(i).residentmem);
        personViewHolder.dataSend.setText("Sent\n" + processList.get(i).data_send);
        personViewHolder.dataRec.setText("Received\n" + processList.get(i).data_rec);
        personViewHolder.dataTotal.setText("Total\n" + processList.get(i).data_tot);
    }

    @Override
    public int getItemCount() {
        return processList.size();
    }

}

